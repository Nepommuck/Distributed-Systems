#ifndef DEMO_ICE
#define DEMO_ICE

module Demo
{
  class MyClass
  {
    int a;
    optional(1) int b;
  }

  interface TestService
  {
    int opInt(int regularIntArg, optional(2) int optionalIntArg);

    int opString(string regularStringArg, optional(2) string optionalStringArg);

    int opClass(MyClass classArg);
  };
};

#endif
