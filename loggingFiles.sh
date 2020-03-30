#!/bin/bash
array=("azs_1" "azs-b_1" "root-ca-mock_1" "root-ca-mock-b_1" "ltca-mock_1" "ltca-mock-b_1" "vehicle-mock_1" "vehicle-mock-b_1")

#mkdir ~/BA_Project/var/logs

for i in ${array[@]}; do
    docker cp "$(docker ps -aqf "name=$i"):/var/logs/." "var/logs"
	echo succeed
done

files=("azsA" "azsB" "ltcaA" "ltcaB" "rcaA" "rcaB" "vehicleA1" "vehicleB1")

for i in ${files[@]}; do
    sed -i 's/^[^:]*://g' ./var/logs/$i.log
    sed -i 's/^[^:]*://g' ./var/logs/$i.log
    sed -i 's/^[^:]*://g' ./var/logs/$i.log
    sed -i 's/ //' ./var/logs/$i.log
done
