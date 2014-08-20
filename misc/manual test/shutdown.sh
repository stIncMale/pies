curl http://localhost:5201/shutdown/ -XPUT -s -S --connect-timeout 1 --max-time 5
echo ""
curl http://localhost:5202/shutdown/ -XPUT -s -S --connect-timeout 1 --max-time 5
echo ""
curl http://localhost:5203/shutdown/ -XPUT -s -S --connect-timeout 1 --max-time 5
echo ""

