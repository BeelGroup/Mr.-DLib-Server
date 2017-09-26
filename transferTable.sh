#!/bin/bash
# Usage: ./transferTable.sh table1 [tables..]
# Execute on the server with the table you want to transfer
# NOTE: No referential integrety check is performed; always copy the whole set of logically interdependent tables

# DONE: use https://stackoverflow.com/a/33576712, https://dev.mysql.com/doc/refman/5.6/en/tablespace-copying.html

# TODO: error handling, i.e. abort on errors with messages for rollback?

# passwords, configs
dst_default=admin@api.mr-dlib.org
dir_default=/home/admin/data/mysql
backup_default=/home/admin/data/backups
transfer_default=/home/admin/data/tmp
src_db_default=mrdlib
dst_db_default=mrdlib
read -s -p "Password for source db: " src_pw
echo
read -s -p "Password for target db: " dst_pw
echo
read -s -p "Password for target server: " dst_ssh_pw
echo
read -p "Source db [$src_db_default]: " src_db
read -p "Target db [$dst_db_default]: " dst_db
read -p "Target server [$dst_default]: " dst
read -p "DB data directory [$dir_default]: " dir
read -p "Backup directory [$backup_default]: " backup_dir
read -p "Transfer directory [$transfer_default]: " transfer_dir
dst=${dst:-$dst_default}
src_db=${src_db:-$src_db_default}
dst_db=${dst_db:-$dst_db_default}
dst=${dst:-$dst_default}
dir=${dir:-$dir_default}
backup_dir=${backup_dir:-$backup_default}
transfer_dir=${transfer_dir:-$transfer_default}

tables="$@"
echo "Preparing backup of tables on destination server..."
flush_stmt=""
for table in $tables
do
	flush_stmt+="FLUSH TABLES $table FOR EXPORT;"
done
SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root $dst_db -e \"$flush_stmt\""


echo "Backing up tablespaces..."
backup_cmd=""
for table in $tables
do
	backup_cmd+="cp $dir/$dst_db/$table.{ibd,cfg} $backup_dir;"
done
SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "$backup_cmd"

SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root $dst_db -e \"UNLOCK TABLES;\""

echo "Preparing tables on source for transfer..."
mysql -p$src_pw -u root $src_db -e "$flush_stmt"

echo "Transferring tables..."
for table in $tables
do
	SSHPASS=$dst_ssh_pw rsync --rsh='sshpass -e ssh' -aP "$dir/$src_db/$table.{ibd,cfg}" "$dst:$transfer_dir"
done

mysql -p$src_pw -u root $src_db -e "UNLOCK TABLES;"

echo "Preparing to swap tables..."
discard_stmt=""
for table in $tables
do
	discard_stmt+="ALTER TABLE $table DISCARD TABLESPACE;"
done
SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root $dst_db -e \"$discard_stmt\""

echo "Importing transferred tables..."
swap_cmd=""
for table in $tables
do
	swap_cmd+="mv $transfer_dir/$table.{ibd,cfg} $dir/$dst_db;"
done
SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "$swap_cmd"

import_stmt=""
for table in $tables
do
	import_stmt+="ALTER TABLE $table IMPORT TABLESPACE;" 
done
SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root $dst_db -e \"$import_stmt\""

echo "Transfer finished."

