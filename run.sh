#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

if [ -x "/usr/local/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home/bin/java" ]; then
    JAVA_BIN="/usr/local/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home/bin/java"
    JAVAC_BIN="/usr/local/Cellar/openjdk@17/17.0.19/libexec/openjdk.jdk/Contents/Home/bin/javac"
elif [ -x "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java" ]; then
    JAVA_BIN="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/java"
    JAVAC_BIN="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home/bin/javac"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
    JAVAC_BIN="$JAVA_HOME/bin/javac"
else
    JAVA_BIN="java"
    JAVAC_BIN="javac"
fi

mkdir -p bin target/classes lib
echo "☕ Đang biên dịch ứng dụng HomuCafe POS..."
find src -name "*.java" > sources.txt
"$JAVAC_BIN" --release 17 -encoding UTF-8 -cp "lib/*:target/classes" -d target/classes @sources.txt
COMPILE_STATUS=$?
rm -f sources.txt

if [ $COMPILE_STATUS -eq 0 ]; then
    cp -r target/classes/* bin/ 2>/dev/null
    echo "✨ Khởi chạy HomuCafe · Coffee POS & Pager System..."
    "$JAVA_BIN" -cp "target/classes:lib/*" App
else
    echo "❌ Lỗi khi biên dịch mã nguồn!"
    exit 1
fi
