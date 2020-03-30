#add Test Data

array=("azs_1" "azs-b_1" "root-ca-mock_1" "root-ca-mock-b_1" "ltca-mock_1" "ltca-mock-b_1" "vehicle-mock_1" "vehicle-mock-b_1")
files=("azsA" "azsB" "ltcaA" "ltcaB" "rcaA" "rcaB" "vehicleA1" "vehicleB1")

for a in 0.25 0.5  0.75 1 1.25 1.5 1.75 2 2.25 2.5 2.75 3 3.25 3.5 3.75 4 4.25 4.5 4.75 5 5.25 5.5 5.75 6; do
       mkdir ~/var/logs/$a ;
       docker-compose down ;
       docker-compose up -d ;
       sleep 55 ;

       curl -i localhost:8920/v1/addData

       curl -i localhost:8100/manipulation

       curl -i localhost:8101/manipulation


       docker run -d -it --rm -v /var/run/docker.sock:/var/run/docker.sock gaiaadm/pumba netem --duration 500s --tc-image gaiadocker/iproute2 loss --percent "$a" baproject_vehicle-mock_1
       for i in {1..1000}; do
               curl -X POST localhost:8901/v1/manipulation;
               sleep 5;
       done;

       for i in ${array[@]}; do
               docker cp "$(docker ps -aqf "name=$i"):/var/logs/." ~/var/logs/$a ;
               echo succeed ;
       done



       for f in ${files[@]}; do
               sed -i 's/^[^:]*://g' ~/var/logs/$a/$f.log ;
               sed -i 's/^[^:]*://g' ~/var/logs/$a/$f.log ;
               sed -i 's/^[^:]*://g' ~/var/logs/$a/$f.log ;
               sed -i 's/ //' ~/var/logs/$a/$f.log ;
       done
       sleep 5;
done
