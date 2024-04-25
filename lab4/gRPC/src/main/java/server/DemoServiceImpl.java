package server;


import Demo.*;
import io.grpc.stub.StreamObserver;

public class DemoServiceImpl extends DemoServiceGrpc.DemoServiceImplBase {
    @Override
    public void opInt(
            IntArgs arguments,
            StreamObserver<Response> responseObserver
    ) {
        System.out.println("opInt with arguments: (" + arguments.getIntArg1() + ", " + arguments.getIntArg2() + ")");

        responseObserver.onNext(Response.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void opString(
            StringArgs arguments,
            StreamObserver<Response> responseObserver
    ) {
        System.out.println("opString with arguments: (" + arguments.getStringArg1() + ", " + arguments.getStringArg2() + ")");

        responseObserver.onNext(Response.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void opEnum(
            EnumArgs arguments,
            StreamObserver<Response> responseObserver
    ) {
        System.out.println("opString with arguments: (" + arguments.getEnumArg1() + ", " + arguments.getEnumArg2() + ")");

        responseObserver.onNext(Response.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
