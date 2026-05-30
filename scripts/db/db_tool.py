#!/usr/bin/env python3
"""
Open-TMS Database Tool
Execute SQL, view table structures, query data, etc.
"""

import sys
import argparse
import pg8000
import json
from typing import Optional, List, Dict, Any

# Database config
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "database": "opentms",
    "user": "opentms",
    "password": "opentms123"
}


def get_connection():
    """Get database connection"""
    try:
        conn = pg8000.connect(**DB_CONFIG)
        return conn
    except Exception as e:
        print(f"[ERROR] Failed to connect: {e}")
        sys.exit(1)


def execute_sql(sql: str, fetch: bool = True) -> Optional[List[Dict]]:
    """Execute SQL and return results"""
    conn = get_connection()
    try:
        cursor = conn.cursor()
        cursor.execute(sql)

        if fetch and cursor.description:
            columns = [desc[0] for desc in cursor.description]
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(dict(zip(columns, row)))
            return result
        else:
            conn.commit()
            return cursor.rowcount if cursor.rowcount > 0 else [{"affected_rows": cursor.rowcount}]
    except Exception as e:
        print(f"[ERROR] SQL execution failed: {e}")
        print(f"[SQL] {sql}")
        return None
    finally:
        cursor.close()
        conn.close()


def list_tables():
    """List all tables"""
    sql = """
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
    ORDER BY table_name
    """
    tables = execute_sql(sql)
    if tables:
        print("\n[*] Database Tables:")
        print("-" * 40)
        for i, t in enumerate(tables, 1):
            print(f"  {i}. {t['table_name']}")
        print()


def describe_table(table_name: str):
    """View table structure"""
    sql = f"""
    SELECT
        column_name,
        data_type,
        is_nullable,
        column_default,
        character_maximum_length
    FROM information_schema.columns
    WHERE table_name = '{table_name}'
    ORDER BY ordinal_position
    """
    columns = execute_sql(sql)
    if columns:
        print(f"\n[*] Table Structure: {table_name}")
        print("-" * 80)
        print(f"{'Column':<25} {'Type':<20} {'Nullable':<8} {'Default':<20}")
        print("-" * 80)
        for col in columns:
            col_type = col['data_type']
            if col['character_maximum_length']:
                col_type += f"({col['character_maximum_length']})"
            print(f"{col['column_name']:<25} {col_type:<20} {col['is_nullable']:<8} {str(col['column_default'] or ''):<20}")
        print()


def query_table(table_name: str, limit: int = 20):
    """Query table data"""
    sql = f"SELECT * FROM {table_name} WHERE deleted = '0' OR deleted IS NULL LIMIT {limit}"
    rows = execute_sql(sql)
    if rows is not None:
        print(f"\n[*] Table Data: {table_name} (top {len(rows)} rows)")
        print("-" * 120)
        if rows:
            headers = list(rows[0].keys())
            print("  " + "  ".join(f"{h:<20}" for h in headers))
            print("-" * 120)
            for row in rows:
                values = [str(row.get(h, '') or '')[:20] for h in headers]
                print("  " + "  ".join(f"{v:<20}" for v in values))
        else:
            print("  (No data)")
        print()


def add_column(table: str, column: str, col_type: str):
    """Add a column"""
    sql = f"ALTER TABLE {table} ADD COLUMN IF NOT EXISTS {column} {col_type}"
    result = execute_sql(sql, fetch=False)
    if result is not None:
        print(f"[OK] Added column: {table}.{column} {col_type}")


def fix_remark_columns():
    """Fix missing remark columns in all tables"""
    check_sql = """
    SELECT table_name FROM information_schema.columns
    WHERE table_schema = 'public' AND column_name = 'remark'
    """
    tables_with_remark = set(r['table_name'] for r in execute_sql(check_sql) or [])

    all_tables_sql = """
    SELECT table_name FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    """
    all_tables = [r['table_name'] for r in execute_sql(all_tables_sql) or []]

    tables_needing_remark = [t for t in all_tables if t not in tables_with_remark]

    print(f"\n[*] Fixing missing remark columns...")
    for table in tables_needing_remark:
        add_column(table, 'remark', 'VARCHAR(500)')

    print("\n[OK] Done fixing remark columns")


def run_sql_file(file_path: str):
    """Execute SQL file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            sql = f.read()

        statements = sql.split(';')
        for i, stmt in enumerate(statements):
            stmt = stmt.strip()
            if stmt and not stmt.startswith('--'):
                print(f"Executing [{i+1}]: {stmt[:60]}...")
                result = execute_sql(stmt, fetch=False)
                if result is None:
                    print(f"  [FAILED]")
                else:
                    print(f"  [OK]")
    except Exception as e:
        print(f"[ERROR] Failed to read file: {e}")


def main():
    parser = argparse.ArgumentParser(description='Open-TMS Database Tool')
    parser.add_argument('--sql', '-s', type=str, help='Execute SQL')
    parser.add_argument('--tables', '-t', action='store_true', help='List all tables')
    parser.add_argument('--desc', '-d', type=str, help='Describe table structure')
    parser.add_argument('--query', '-q', type=str, help='Query table data')
    parser.add_argument('--limit', '-l', type=int, default=20, help='Query limit (default 20)')
    parser.add_argument('--add-col', nargs=3, metavar=('TABLE', 'COLUMN', 'TYPE'), help='Add column')
    parser.add_argument('--fix-remark', '-f', action='store_true', help='Fix missing remark columns')
    parser.add_argument('--run-file', '-r', type=str, help='Execute SQL file')

    args = parser.parse_args()

    if args.tables:
        list_tables()
    elif args.desc:
        describe_table(args.desc)
    elif args.query:
        query_table(args.query, args.limit)
    elif args.sql:
        result = execute_sql(args.sql)
        if result is not None:
            print(json.dumps(result, indent=2, default=str))
    elif args.add_col:
        add_column(args.add_col[0], args.add_col[1], args.add_col[2])
    elif args.fix_remark:
        fix_remark_columns()
    elif args.run_file:
        run_sql_file(args.run_file)
    else:
        parser.print_help()
        print("\n[*] Examples:")
        print("  python db_tool.py -t                           # List tables")
        print("  python db_tool.py -d tms_country_t             # Describe table")
        print("  python db_tool.py -q tms_country_t             # Query table")
        print("  python db_tool.py -s 'SELECT * FROM tms_country_t LIMIT 5'")
        print("  python db_tool.py -f                           # Fix remark columns")
        print("  python db_tool.py -r db/schema/fix.sql         # Run SQL file")


if __name__ == "__main__":
    main()