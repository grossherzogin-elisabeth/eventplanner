export function saveBlobToFile(fileName: string, data: Blob) {
    try {
        const downloadElement = document.createElement('a');
        downloadElement.setAttribute('download', fileName);
        document.body.appendChild(downloadElement);
        downloadElement.style.display = 'none';
        const url = window.URL.createObjectURL(data);
        downloadElement.href = url;
        downloadElement.download = fileName;
        downloadElement.click();
        window.URL.revokeObjectURL(url);
    } catch (e) {
        console.error(e);
    }
}

export function saveStringToFile(fileName: string, data: string) {
    const blob = new Blob([data], { type: 'octet/stream' });
    saveBlobToFile(fileName, blob);
}
