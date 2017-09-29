#!/bin/env python3
import sys
import os
import argparse

CLIPBOARD = False
try:
    import pyperclip
    CLIPBOARD = True
except ImportError:
    print("Execute 'pip3 install --user pyperclip' to enable automatic copying of commands to clipboard")

BACKUP_DIR="/home/admin/data/backups"
TRANSFER_DIR="/home/admin/data/tmp"
DATA_DIR="/home/admin/data/mysql"
DATABASE="mrdlib"
SOURCE="admin@api-dev.mr-dlib.org"
TARGET="admin@api-beta.mr-dlib.org"

def exec(machine, console, command):
    return (f"[{machine}] {console} > {command}", command)

def cmd(machine, command):
    return exec(machine, "bash", command)

def sql(machine, command):
    return exec(machine, "mysql", command)

def flush(machine, tables):
    table_list = ', '.join(tables)
    return sql(machine, f"FLUSH TABLES {table_list} FOR EXPORT;")

def unlock(machine):
    return sql(machine, "UNLOCK TABLES;")

def discard_table(machine, tables):
    statements = [ f"ALTER TABLE {table} DISCARD TABLESPACE;" for table in tables]
    statements.insert(0, "SET foreign_key_checks=0;")
    return sql(machine, ' '.join(statements))

def import_table(machine, tables):
    statements = [ f"ALTER TABLE {table} IMPORT TABLESPACE;" for table in tables]
    statements.insert(0, "SET foreign_key_checks=0;")
    return sql(machine, ' '.join(statements))

def absolute_files(directory, tables):
    append = lambda ext: lambda name: f"{name}.{ext}"
    prepend = lambda prefix: lambda name: f"{prefix}{name}"
    files = list(map(append('ibd'), tables)) + list(map(append('cfg'), tables))
    absolute = list(map(prepend(directory + os.sep), files))
    return absolute

def backup(machine, tables):
    absolute = absolute_files(DATA_DIR + os.sep + DATABASE, tables)
    file_list = ' '.join(absolute)
    return cmd(machine, f"cp {file_list} {BACKUP_DIR}")

def delete_backup(machine, tables):
    absolute = absolute_files(BACKUP_DIR, tables)
    file_list = ' '.join(absolute)
    return cmd(machine, f"rm {file_list}")

def transfer(source, target, tables):
    absolute = absolute_files(BACKUP_DIR, tables)
    file_list = ' '.join(absolute)
    return cmd(source, f"rsync -P {file_list} {target}:{TRANSFER_DIR}")

def swap(machine, tables):
    absolute = absolute_files(TRANSFER_DIR, tables)
    file_list = ' '.join(absolute)
    return cmd(machine, f"chown mysql {file_list}; chgrp mysql {file_list}; mv {file_list} {DATA_DIR + os.sep + DATABASE}")

def main():
    global SOURCE, TARGET, DATABASE, CLIPBOARD
    parser = argparse.ArgumentParser(description="Transfer database tables between servers.")
    parser.add_argument('tables', metavar='table_name', type=str, nargs='+', help='Tables to copy')
    parser.add_argument('--source', type=str, help='Source machine', default=SOURCE, required=False)
    parser.add_argument('--target', type=str, help='Target machine', default=TARGET, required=False)
    parser.add_argument('--database', type=str, help='Database to use', default=DATABASE, required=False)
    parser.add_argument('--disable-clipboard', dest='clipboard', action='store_true', help='Disable copying to clipboard', default=False, required=False)
    args = parser.parse_args()
    tables = args.tables
    SOURCE = args.source
    TARGET = args.target
    DATABASE = args.database
    if CLIPBOARD:
        CLIPBOARD = not args.clipboard
    print("Open four terminals and keep them open during the whole process: ")
    print("MySQL on source & target and bash on source & target, as root")
    print("Enter the following commands in the respective terminals.")
    print("Always wait for completion of the previous command before progressing.") 
    instructions = [
        flush(TARGET, tables),
        backup(TARGET, tables),
        unlock(TARGET),
        flush(SOURCE, tables),
        backup(SOURCE, tables),
        unlock(SOURCE),
        transfer(SOURCE, TARGET, tables),
        discard_table(TARGET, tables),
        swap(TARGET, tables),
        import_table(TARGET, tables)
    ]
    for step, command in instructions:
        print(step)
        if CLIPBOARD:
            pyperclip.copy(command)
            input("Press [Enter] to copy next step to clipboard.")

    print("You can now delete the backed up tables.")
    print(delete_backup(SOURCE, tables)[0])
    print(delete_backup(TARGET, tables)[0])

if __name__ == '__main__':
    main()
