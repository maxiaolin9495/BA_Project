#add Test Data
echo "add Test Data $(curl -i localhost:8920/v1/addData)"

echo "startup VehicleA1 $(curl -i localhost:8100/manipulation)"

echo "startup VehicleB1 $(curl -i localhost:8101/manipulation)"