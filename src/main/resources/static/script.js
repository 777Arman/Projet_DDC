document.getElementById("b_envoie").addEventListener("click", function (e) {
    if (e) e.preventDefault();

    const cvFile = document.getElementById("CV").files[0];
    const lmFile = document.getElementById("motiv").files[0];
    const poste = document.getElementById("selection_poste").value;
    const stockage = document.getElementById("stockage").checked;
    const partage = document.getElementById("partage").checked;

    const formData = new FormData();
    formData.append("cv", cvFile);
    formData.append("lm", lmFile);
    formData.append("poste", poste);
    formData.append("stockage", stockage);
    formData.append("partage", partage);

    fetch("/upload", {
        method: "POST",
        body: formData
    })
    .then(async response => {
        const text = await response.text();
        if (response.ok) {
            alert("Vos fichiers ont bien été envoyés !");
        } else {
            alert("Erreur lors de l’envoi : " + text);
        }
    })
    .catch(() => alert("Impossible de contacter le serveur."));
});