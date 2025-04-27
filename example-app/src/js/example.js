import { SecureStorage } from 'capacitor-secure-storage';

// Create global functions that can be accessed from HTML
window.storeSecureData = async () => {
    try {
        const key = document.getElementById('storeKey').value.trim();
        const value = document.getElementById('storeValue').value;
        
        if (!key) {
            setStatus('Error: Key cannot be empty', 'error');
            return;
        }
        
        await SecureStorage.set({
            key: key,
            value: value
        });
        
        setStatus(`Successfully stored value for key: "${key}"`, 'success');
        document.getElementById('storeKey').value = '';
        document.getElementById('storeValue').value = '';
        
        // Refresh the key list
        await listAllKeys();
    } catch (error) {
        setStatus(`Error storing data: ${error.message}`, 'error');
    }
};

window.retrieveSecureData = async () => {
    try {
        const key = document.getElementById('retrieveKey').value.trim();
        
        if (!key) {
            setStatus('Error: Key cannot be empty', 'error');
            return;
        }
        
        const result = await SecureStorage.get({ key: key });
        const outputElement = document.getElementById('retrieveOutput');
        
        if (result.value === null) {
            outputElement.textContent = `No value found for key: "${key}"`;
        } else {
            outputElement.textContent = `Value: ${result.value}`;
        }
        
        outputElement.style.display = 'block';
        setStatus(`Retrieved value for key: "${key}"`, 'success');
    } catch (error) {
        setStatus(`Error retrieving data: ${error.message}`, 'error');
    }
};

window.removeSecureData = async (key) => {
    try {
        await SecureStorage.remove({ key: key });
        setStatus(`Successfully removed key: "${key}"`, 'success');
        
        // Refresh the key list
        await listAllKeys();
    } catch (error) {
        setStatus(`Error removing data: ${error.message}`, 'error');
    }
};

window.viewSecureData = async (key) => {
    try {
        document.getElementById('retrieveKey').value = key;
        await retrieveSecureData();
    } catch (error) {
        setStatus(`Error viewing data: ${error.message}`, 'error');
    }
};

window.clearAllData = async () => {
    try {
        if (confirm('Are you sure you want to delete all stored data? This cannot be undone.')) {
            await SecureStorage.clear();
            setStatus('Successfully cleared all secure storage data', 'success');
            
            // Refresh the key list
            await listAllKeys();
            
            // Clear the retrieve output
            document.getElementById('retrieveOutput').style.display = 'none';
        }
    } catch (error) {
        setStatus(`Error clearing data: ${error.message}`, 'error');
    }
};

window.listAllKeys = async () => {
    try {
        const result = await SecureStorage.keys();
        const keysList = document.getElementById('storedKeys');
        keysList.innerHTML = '';
        
        if (result.keys.length === 0) {
            keysList.innerHTML = '<li>No keys stored</li>';
            return;
        }
        
        result.keys.forEach(key => {
            const li = document.createElement('li');
            
            const keySpan = document.createElement('span');
            keySpan.textContent = key;
            li.appendChild(keySpan);
            
            const actionsDiv = document.createElement('div');
            actionsDiv.className = 'key-actions';
            
            const viewButton = document.createElement('button');
            viewButton.className = 'secondary';
            viewButton.textContent = 'View';
            viewButton.onclick = () => viewSecureData(key);
            actionsDiv.appendChild(viewButton);
            
            const deleteButton = document.createElement('button');
            deleteButton.className = 'danger';
            deleteButton.textContent = 'Delete';
            deleteButton.onclick = () => removeSecureData(key);
            actionsDiv.appendChild(deleteButton);
            
            li.appendChild(actionsDiv);
            keysList.appendChild(li);
        });
        
        setStatus(`Retrieved ${result.keys.length} keys from secure storage`, 'success');
    } catch (error) {
        setStatus(`Error listing keys: ${error.message}`, 'error');
    }
};

// Helper function to set status messages
function setStatus(message, type = 'info') {
    const statusElement = document.getElementById('statusOutput');
    statusElement.textContent = message;
    
    // Reset the status styling
    statusElement.style.borderLeftColor = '#3880ff';
    
    // Apply styling based on message type
    if (type === 'error') {
        statusElement.style.borderLeftColor = '#eb445a';
    } else if (type === 'success') {
        statusElement.style.borderLeftColor = '#2dd36f';
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Load initial keys
        await listAllKeys();
    } catch (error) {
        setStatus(`Error initializing app: ${error.message}`, 'error');
    }
});
