# migration_etl.py # migration_etl.py — Load legacy CSV into refactored appointments schema
# Transformations: T1 (date parse), T2 (room split), T3 (drop redundant cols), T4 (status validation), T5 (decimal cast)

import csv
import mysql.connector
from datetime import datetime

# T4: Only these five codes are valid in the refactored appt_status_ref table
VALID_STATUSES = {'P', 'C', 'X', 'H', 'R'}

# ── T1: Parse legacy date string to Python datetime ──────────────────────
def parse_appt_date(raw):
    """
    Legacy format: 'DD/MM/YYYY HH:MM' (e.g. '15/03/2024 09:30')
    Target:        Python datetime object → MySQL DATETIME string
    """
    try:
        # strptime parses the exact legacy format; raises ValueError on mismatch
        dt = datetime.strptime(raw.strip(), '%d/%m/%Y %H:%M')
        # strftime converts to MySQL-compatible DATETIME string
        return dt.strftime('%Y-%m-%d %H:%M:%S')
    except ValueError:
        # Return None so the calling code can detect and skip the row
        return None

# ── T2: Split 'Room 3 Block B' into (3, 'Block B') ──────────────────────
def split_room(raw):
    """
    Legacy format: 'Room <number> <block_label>'
    Returns:       (room_number: int, building_block: str)
    """
    try:
        parts = raw.strip().split()   # ['Room', '3', 'Block', 'B']
        room_number = int(parts[1])   # Extract integer room number
        # Rejoin everything after the room number as the block identifier
        building_block = ' '.join(parts[2:])   # 'Block B'
        return room_number, building_block
    except (IndexError, ValueError):
        # If format is unexpected, return sentinel values and log
        return None, None

# ── Main ETL function ────────────────────────────────────────────────────
def migrate(csv_path, db_config):
    # Connect to MySQL; autocommit=False enables transaction rollback on error
    conn = mysql.connector.connect(**db_config)
    conn.autocommit = False
    cursor = conn.cursor()

    skipped = []      # Accumulates appt_ids of rejected rows
    inserted = 0      # Counter for successfully inserted rows

    try:
        with open(csv_path, newline='', encoding='utf-8') as f:
            reader = csv.DictReader(f)   # Reads header row as column names

            for row in reader:

                # ── T4: Validate status code ────────────────────────
                if row['status'] not in VALID_STATUSES:
                    # Log the bad row; do not insert it
                    skipped.append({'appt_id': row['appt_id'], 'reason': f"Unknown status: {row['status']}"})
                    continue

                # ── T1: Parse appt_date ─────────────────────────────
                appt_dt = parse_appt_date(row['appt_date'])
                if appt_dt is None:
                    skipped.append({'appt_id': row['appt_id'], 'reason': 'Unparseable date'})
                    continue

                # ── T2: Split room column ───────────────────────────
                room_no, block = split_room(row['room'])
                if room_no is None:
                    skipped.append({'appt_id': row['appt_id'], 'reason': 'Unparseable room'})
                    continue

                # ── T5: Cast fee and discount to Decimal-safe strings
                fee      = round(float(row['fee']),      2)
                discount = round(float(row['discount']), 2)

                # ── T3: Omit patient_nm, patient_ph, doc_name, net_fee ──
                # These columns are redundant after normalisation:
                #   patient_nm / patient_ph → derived via FK on patient_id
                #   doc_name               → derived via FK on doc_id
                #   net_fee                → computed as fee - discount
                cursor.execute(
                    '''INSERT INTO appointments
                       (appt_id, patient_id, doc_id, appt_date,
                        status, fee, discount, room_number, building_block)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)''',
                    (row['appt_id'], row['patient_id'], row['doc_id'],
                     appt_dt, row['status'], fee, discount, room_no, block)
                )
                inserted += 1

        conn.commit()   # Commit transaction only after all rows processed
        print(f'Migration complete: {inserted} rows inserted.')
        print(f'Skipped {len(skipped)} rows:')
        for s in skipped:
            print(f"  appt_id={s['appt_id']}: {s['reason']}")

    except Exception as e:
        conn.rollback()   # Roll back entire transaction on unexpected error
        print(f'Migration failed — transaction rolled back. Error: {e}')
        raise

    finally:
        cursor.close()
        conn.close()

# ── Database connection configuration ────────────────────────────────────
DB_CONFIG = {
    'host':     'localhost',
    'port':     3306,
    'user':     'nouman',
    'password': 'mypassword123',
    'database': 'healthbridge'
}

# ── Entry point ──────────────────────────────────────────────────────────
if __name__ == '__main__':
    migrate('legacy_appointments.csv', DB_CONFIG)
