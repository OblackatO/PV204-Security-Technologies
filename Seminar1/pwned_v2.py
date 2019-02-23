# https://haveibeenpwned.com/API/v2

import pwnedpasswords
import requests
import hashlib
import matplotlib.pyplot as plt


def load_passwords(file_name: str) -> dict:
    """
    Function that loads most common passwords to memory.
    """
    with open(file_name, 'r') as f:
        passwords = f.read().split('\n')
    pwds_dict = dict()
    for pwd in passwords:
        pwd = pwd.split()
        pwds_dict[pwd[1]] = pwd[2]
    return pwds_dict
        
def build_plot(passwords_frequency: dict):
    """
    Builds plot, showing usage frequency, in numbers, of most common
    used passwords.
    """
    plt.bar(range(len(passwords_frequency)), list(passwords_frequency.values()), align='center')
    plt.xticks(range(len(passwords_frequency)), list(passwords_frequency.keys()))
    plt.show()

def check_password_frequency(passwords_to_test: dict) -> dict:
    """
    Checks password usage frequency.
    """
    passwords_frequency = dict()
    for password in passwords_to_test:
        digest = hashlib.sha1(password.encode("utf8")).hexdigest().upper()
        prefix = digest[0:5]
        suffix = digest[5:]
        response = requests.get("https://api.pwnedpasswords.com/range/" + prefix)
        for record in response.text.upper().split("\r\n"):
            hashed, count = record.split(":")
            if suffix == hashed:
                print("Password: {} -- x{} times".format(password, count))
                passwords_frequency[password] = count
    return passwords_frequency


def main():
    pwds_frequency = check_password_frequency(load_passwords("passwords.txt"))
    build_plot(pwds_frequency)
    

if __name__ == "__main__":
    main()
