import { SecureStorage } from 'capacitor-secure-storage';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    SecureStorage.echo({ value: inputValue })
}
