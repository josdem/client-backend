for i in src/test/java/com/all/backend/commons/signup/*.java
do
cat /home/josdem/Desktop/LICENSE.txt $i >> $i.copy
mv $i.copy $i
done
