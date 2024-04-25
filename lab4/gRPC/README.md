### Generate Java
```
PROTOC_JAVA_DST="src/main/java/"
    
protoc -I. --java_out=$PROTOC_JAVA_DST \
  --plugin=protoc-gen-grpc-java=protoc-gen-grpc-java.exe \
  --grpc-java_out=$PROTOC_JAVA_DST \
  proto/Demo.proto
```

### Generate Python
```
PROTOC_PYTHON_DST="client/"

python -m grpc_tools.protoc -I. \
--python_out=$PROTOC_PYTHON_DST \
--pyi_out=$PROTOC_PYTHON_DST \
--grpc_python_out=$PROTOC_PYTHON_DST \
  proto/Demo.proto
```

### Clear all generated files
```
rm -rf src/main/java/Demo/
rm -rf client/proto/
```
