import pexpect
import sys

"""
with open("wordlist.txt", "r") as file1:
    for pwd in file1.readlines():
        try:
            child = pexpect.spawn("cryptsetup tcryptDump pv204_assignment.tc --veracrypt --dump-master-key")
            result = child.expect("Enter passphrase for pv204_assignment.tc:")
            child.sendline(pwd.strip())
            result = child.expect(["No device header detected with this passphrase.", "EOF"])
            print("Tried: {}".format(pwd))
        except Exception as identifier:
            if "EOF" in str(identifier):
                print("Match found: {}".format(pwd))
                break
            print("Tried: {}".format(pwd))
"""
with open("wordlist.txt", "r") as file1:
    pwds = file1.readlines()
    for pwd in pwds:
        child = pexpect.spawn("cryptsetup tcryptDump pv204_assignment.tc --veracrypt --dump-master-key")
        result = child.expect("Enter passphrase for pv204_assignment.tc:")
        child.sendline(pwd.strip())
        result = child.expect(["No device header detected with this passphrase.", "EOF"])
        child.close()
        print("Tried: {}".format(pwd))