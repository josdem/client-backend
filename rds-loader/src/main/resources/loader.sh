#!/bin/bash


buildClassPath() {
        jar_dir=$1
        if [ $# -ne 1 ]; then
                echo "Jar directory must be specified."
                exit 1
        fi
        class_path=
        c=1
        for i in `ls $jar_dir/*.jar`
        do
                if [ "$c" -eq "1" ]; then
                        class_path=${i}
                        c=2
                else
                        class_path=${class_path}:${i}
                fi
        done
        echo $class_path
        #return $class_path
}

dir=`pwd`/System/Jar

CP=`buildClassPath $dir`
 

java -Xmx1024m -Xms1024m -cp $CP com.all.rds.loader.Loader $@