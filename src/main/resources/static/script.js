document.getElementById("formulaire").addEventListener("submit", function(e) {
    e.preventDefault();

    const cvFile = document.getElementById("CV").files[0];
    const lmFile = document.getElementById("motiv").files[0];
    const poste = document.getElementById("selection_poste").value;
    const stockage = document.getElementById("stockage").checked;
    const partage = document.getElementById("partage").checked;

    // Vérifications
    if (!cvFile) {
        alert("Veuillez sélectionner un CV");
        return;
    }
    if (!lmFile) {
        alert("Veuillez sélectionner une lettre de motivation");
        return;
    }
    if (!poste) {
        alert("Veuillez sélectionner un poste");
        return;
    }

    const formData = new FormData();
    formData.append("cv", cvFile);
    formData.append("lm", lmFile);
    formData.append("poste", poste);
    formData.append("stockage", stockage);
    formData.append("partage", partage);

    console.log("Envoi des données:", {
        cv: cvFile.name,
        lm: lmFile.name,
        poste: poste,
        stockage: stockage,
        partage: partage
    });

    fetch("/upload", {
        method: "POST",
        body: formData
    })
    .then(async response => {
        const text = await response.text();
        console.log("Réponse du serveur:", text);
        return text;
    })
    .then(text => {
        if (text.includes("succès") || text.includes("enregistrée")) {
            alert("Vos fichiers ont bien été envoyés !");
            document.getElementById("formulaire").reset();
        } else {
            alert("Réponse du serveur : " + text);
        }
    })
    .catch(error => {
        console.error("Erreur complète:", error);
        alert("Erreur lors de l'envoi : " + error.message);
    });
});