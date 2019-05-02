import itertools
x = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

with open("wordlist.txt", "a") as file1:
    for p in itertools.product(x, repeat=3):
        file1.write("pv204_"+str(p[0])+str(p[1])+str(p[2])+"\n")
