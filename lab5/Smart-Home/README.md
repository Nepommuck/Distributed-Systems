## Generate interfaces

### Server - Java
1. Download `protoc-gen-grpc-java-[VERSION]-[OS].exe` from [maven.org](https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.62.2/)
2. Rename it into `protoc-gen-grpc-java.exe` and place in `Smart-Home` directory
3. Run (in `Smart-Home` directory):
    ```
    PROTOC_JAVA_DST="Smart-Home-Server/src/main/java"
    
    protoc -I. --java_out=$PROTOC_JAVA_DST \
      --plugin=protoc-gen-grpc-java=protoc-gen-grpc-java.exe \
      --grpc-java_out=$PROTOC_JAVA_DST \
      $(find proto -iname '*.proto')
    ```

### Client - Python
1. Install `grpcio-tools`:
   ```
   pip install grpcio-tools
   ```
2. Run (in `Smart-Home` directory):
   ```
   PROTOC_PYTHON_DST="Smart-Home-Client"
   
   python -m grpc_tools.protoc -I. \
   --python_out=$PROTOC_PYTHON_DST \
   --pyi_out=$PROTOC_PYTHON_DST \
   --grpc_python_out=$PROTOC_PYTHON_DST \
     $(find proto -iname '*.proto')
   ```
   
## Run Server
Execute `main()` from `Smart-Home-Server/src/main/java/server/Main.java`

## Run Client
   ```
   python Smart-Home-Client/main.py
   ```
