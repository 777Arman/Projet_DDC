document.getElementById("b_envoie").addEventListener("click", function() {
    const verif_cv = document.getElementById("CV");
    const verif_lm = document.getElementById("motiv");
    if(verif_cv.files.length === 0){
        alert("Veuillez rajouter un CV")
    }
    else {
        if(verif_lm.files.length === 0){
            alert("Veuillez rajouter une lettre de motivation")
        }
        else{
            alert("Vos fichiers ont bien été envoyés !");
        }
    }
});