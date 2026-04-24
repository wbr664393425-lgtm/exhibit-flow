#!/usr/bin/env python3
"""Import pig.sql into exhibit_flow database using pymysql."""
import pymysql

SQL_FILE = '/Users/wbr/Documents/project/exhibit-flow/devolop-master/pig.sql'

with open(SQL_FILE, 'r', encoding='utf-8') as f:
    sql_content = f.read()

conn = pymysql.connect(
    host='127.0.0.1',
    port=3306,
    user='root',
    password='w6316278',
    database='exhibit_flow',
    charset='utf8mb4',
    autocommit=True
)

try:
    cursor = conn.cursor()
    
    # Split by semicolons and execute each statement
    # Remove comments and empty lines for cleaner parsing
    statements = []
    current = []
    for line in sql_content.split('\n'):
        stripped = line.strip()
        # Skip comment lines
        if stripped.startswith('--') or stripped.startswith('/*') or stripped.startswith(' *') or stripped == '':
            if stripped.startswith('/*') or stripped.startswith(' *') or stripped.endswith('*/'):
                continue
            continue
        current.append(line)
        if stripped.endswith(';'):
            stmt = '\n'.join(current)
            if stmt.strip() not in ('SET NAMES utf8mb4;', 'SET FOREIGN_KEY_CHECKS = 0;', 'SET FOREIGN_KEY_CHECKS = 1;', 'COMMIT;', 'BEGIN;'):
                statements.append(stmt)
            current = []
    
    # Handle any remaining content
    if current:
        remaining = '\n'.join(current).strip()
        if remaining and remaining != ';':
            statements.append(remaining)
    
    print(f"Found {len(statements)} statements to execute")
    
    for i, stmt in enumerate(statements):
        try:
            cursor.execute(stmt)
            if (i + 1) % 20 == 0:
                print(f"  Executed {i + 1}/{len(statements)} statements...")
        except Exception as e:
            print(f"  Error at statement {i + 1}: {str(e)[:200]}")
            print(f"  Statement preview: {stmt[:150]}...")
    
    print(f"\nDone! Executed {len(statements)} statements.")
    
    # Verify key tables exist
    cursor.execute("SHOW TABLES")
    tables = [row[0] for row in cursor.fetchall()]
    print(f"\nTables created ({len(tables)}): {', '.join(sorted(tables))}")
    
    required = ['sys_user', 'sys_role', 'sys_menu', 'sys_dict']
    for t in required:
        if t in tables:
            print(f"  ✓ {t} exists")
        else:
            print(f"  ✗ {t} MISSING!")

finally:
    conn.close()
