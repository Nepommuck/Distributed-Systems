# from time import time
import time
import ray

@ray.remote
def do_sth():
    time.sleep(3)
    print("Finished do sth")
    return "ss"


ids = [do_sth.remote() for _ in range(5)]

print("Before wait")
[ready], remainings = ray.wait(ids, num_returns=1)
print("After wait")

result = ray.get(ready)

print("After get", result)
