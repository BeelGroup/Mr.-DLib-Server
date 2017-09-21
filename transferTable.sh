#!/bin/bash
# Usage: ./transferTable.sh table1 [tables..]
# Execute on the server with the table you want to transfer

# TODO: error handling, i.e. abort on errors with messages for rollback?

# passwords, configs
src_default=admin@api-dev.mr-dlib.org
dst_default=admin@api.mr-dlib.org
dir_default=/home/admin/data/dumps
read -s -p "Password for source db: " src_pw
echo
read -s -p "Password for target db: " dst_pw
echo
read -s -p "Password for target server: " dst_ssh_pw
echo
#read -p "Source server [$src_default]: " src
read -p "Target server [$dst_default]: " dst
read -p "Save directory [$dir_default]: " dir
#src=${src:-$src_default}
dst=${dst:-$dst_default}
dir=${dir:-$dir_default}

function tablesync {
	table=$1
	echo "Starting transfer of $table."
	# dump relevant table; no locks, no drop table
	echo "Dumping $table..."
	mysqldump -p$src_pw -u root --compact --result-file=$dir/$table.sql mrdlib $table
	# check for constraint symbols and other stuff that could inhibit this
	#vim $dir/$table.sql
	echo "Fixing constraint symbols..."
	sed -i "s/CONSTRAINT \`[^\`]+\`/CONSTRAINT /g" $dir/$table.sql
	# recreate as table2
	echo "Changing table name: $table -> ${table}2"
	sed -i "s/ \`$table\`/ \`${table}2\`/g" $dir/$table.sql
	# copy it
	echo "Copying dump..."
	SSHPASS=$dst_ssh_pw rsync --rsh='sshpass -e ssh' -aP $dir/$table.sql $dst:$dir
	# import it
	echo "Importing dump..."
	SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root mrdlib <$dir/$table.sql"
	# delete old backups
	echo "Deleting old backup: ${table}_backup..."
	drop_stmt="DROP TABLE IF EXISTS ${table}_backup;"
	SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root mrdlib -e \"$drop_stmt\""
	# exchange tables
	echo "Swapping tables $table <-> ${table}2"
	rename_stmt="RENAME TABLE $table TO ${table}_backup; RENAME TABLE ${table}2 TO $table;"
	SSHPASS=$dst_ssh_pw sshpass -e ssh -t "$dst" "mysql -p$dst_pw -u root mrdlib -e \"$rename_stmt\""
	echo "Finished transfering $table."
}

for table_name in "$@"
do
	tablesync "$table_name"
done

