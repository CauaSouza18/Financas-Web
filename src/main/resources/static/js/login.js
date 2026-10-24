const formulario = document.querySelector("#form-login");
const campoEmail = document.querySelector("#email");
const campoSenha = document.querySelector("#password");
const mensagemErro = document.querySelector("#erro-login");


formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    mensagemErro.hidden = true;
    mensagemErro.textContent = "";

    const email = campoEmail.value;
    const senha = campoSenha.value;

    try {
        const resposta = await fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, senha })
        });

        if (resposta.ok) {
            window.location.href = "/html/menu.html";
            return;
        }

        mensagemErro.textContent = "E-mail ou senha inválidos";
    } catch (erro) {
        console.error("Falha ao enviar login:", erro);
        mensagemErro.textContent = "Não foi possível conectar. Tente novamente.";
    }

    mensagemErro.hidden = false;
});
