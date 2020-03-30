#add Test Data
array=("azs_1" "azs-b_1" "root-ca-mock_1" "root-ca-mock-b_1" "ltca-mock_1" "ltca-mock-b_1" "vehicle-mock_1" "vehicle-mock-b_1")
files=("azsA" "azsB" "ltcaA" "ltcaB" "rcaA" "rcaB" "vehicleA1" "vehicleB1")




for a in 10 20 30 40 50 60 70 80 90 100 110 120 130 140 150 160 170 180 190 200 ; do
		mkdir ~/var/logs/$a ;
		docker-compose down ;
		docker-compose up -d ;
		sleep 55 ;

       curl -i localhost:8920/v1/addData

       curl -i localhost:8100/manipulation

       curl -i localhost:8101/manipulation


      docker run -d -it --rm -v /var/run/docker.sock:/var/run/docker.sock gaiaadm/pumba netem --duration 420s --tc-image gaiadocker/iproute2 delay --time "$a" --jitter 0 --distribution normal baproject_vehicle-mock_1
      for i in {1..500}; do
               curl -X POST localhost:8901/v1/manipulation;
               sleep 1;
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

for a in 210 220 230 240 250 260 270 280 290 300 310 320 330 340 350 360 370 380 390 400; do
        mkdir ~/var/logs/$a ;
        docker-compose down ;
        docker-compose up -d ;


       sleep 55 ;

        curl -i localhost:8920/v1/addData

        curl -i localhost:8100/manipulation

        curl -i localhost:8101/manipulation



        docker run -d -it --rm -v /var/run/docker.sock:/var/run/docker.sock gaiaadm/pumba netem --duration 520s --tc-image gaiadocker/iproute2 delay --time "$a" --jitter 0 --distribution normal baproject_vehicle-mock_1
        for i in {1..500}; do
                curl -X POST localhost:8901/v1/manipulation;
                sleep 4;
        done
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

