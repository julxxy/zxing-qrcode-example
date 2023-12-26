#!/bin/bash

current_user=$(whoami)
if [[ $current_user == "weasley" ]]; then
  export JAVA_HOME=$(/usr/libexec/java_home -v 21)
  export PATH=$JAVA_HOME/bin:$PATH
fi

APP_NAME="zxing-qrcode-example"

# 编译
mvn clean package -Dmaven.test.skip=true
mv -fv target/$APP_NAME.jar ./$APP_NAME.jar && mvn clean
# 运行
java -jar $APP_NAME.jar

: '
java -jar zxing-qrcode-example.jar
'
