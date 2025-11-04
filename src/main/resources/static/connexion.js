const form = document.getElementById('connexionForm');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');

form.addEventListener('submit', async function(e) {
    e.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();

    errorMessage.style.display = 'none';
    successMessage.style.display = 'none';

    if (!email || !password) {
        showError('Veuillez remplir tous les champs');
        return;
    }

    try {
        const response = await fetch('/connexion', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: email, motDePasse: password })
        });

        if (response.ok) {
            showSuccess('Connexion réussie !');
            setTimeout(() => {
                window.location.href = '/AccueilRH';
            }, 800);
        } else {
            const msg = await response.text();
            showError(msg || 'Identifiants incorrects');
        }
    } catch (error) {
        console.error('Erreur de connexion:', error);
        showError('Erreur serveur. Réessayez plus tard.');
    }
});

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function showSuccess(message) {
    successMessage.textContent = message;
    successMessage.style.display = 'block';
}