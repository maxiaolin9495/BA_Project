#!/bin/bash
array=("azs_1" "azs-b_1" "root-ca-mock_1" "root-ca-mock-b_1" "ltca-mock_1" "ltca-mock-b_1" "vehicle-mock_1" "vehicle-mock-b_1")

#mkdir ~/BA_Project/var/logs

for i in ${array[@]}; do
    docker cp "$(docker ps -aqf "name=$i"):/var/logs/." "var/logs"
	echo succeed
done