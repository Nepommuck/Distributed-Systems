package sr.ice.server;

import Demo.MyClass;
import Demo.TestService;
import com.zeroc.Ice.Current;

import java.util.Optional;
import java.util.OptionalInt;

public class TestServiceImpl implements TestService {
//	private static final long serialVersionUID = -2448962912780867770L;

    @Override
    public int opInt(int regularIntArg, OptionalInt optionalIntArg, Current current) {
        System.out.println("opInt with arguments: (" + regularIntArg + ", " + optionalIntArg + ")");
        return 0;
    }

    @Override
    public int opString(String regularStringArg, Optional<String> optionalStringArg, Current current) {
        System.out.println("opString with arguments: (" + regularStringArg + ", " + optionalStringArg + ")");
        return 0;
    }

    @Override
    public int opClass(MyClass classArg, Current current) {
        System.out.println("opClass with argument: MyClass(a=" + classArg.a + ", optionalB=" + classArg.optionalB() + ")");
        return 0;
    }
}
