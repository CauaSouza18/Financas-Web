const formularioRecuperacao = document.querySelector("#form-recuperacao");
const campoEmailRecuperacao = document.querySelector("#email");
const mensagemRecuperacao = document.querySelector("#mensagem-recuperacao");
const botaoRecuperacao = document.querySelector("#botao-recuperacao");

formularioRecuperacao.addEventListener("submit", async evento => {
    evento.preventDefault();
    mensagemRecuperacao.hidden = true;
    mensagemRecuperacao.className = "feedback-message";
    botaoRecuperacao.disabled = true;

    try {
        const resposta = await fetch("/api/recuperar-senha", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: campoEmailRecuperacao.value.trim() })
        });

        if (resposta.ok) {
            mensagemRecuperacao.textContent = "Se este e-mail estiver cadastrado, enviaremos um link de recuperação. Confira também o spam.";
            mensagemRecuperacao.classList.add("success");
        } else if (resposta.status === 503) {
            mensagemRecuperacao.textContent = "O envio de e-mail ainda não está configurado no servidor. Tente novamente mais tarde.";
            mensagemRecuperacao.classList.add("error");
        } else {
            mensagemRecuperacao.textContent = "Não foi possível solicitar a recuperação. Confira o e-mail e tente novamente.";
            mensagemRecuperacao.classList.add("error");
        }
    } catch (erro) {
        console.error("Falha ao solicitar recuperação de senha:", erro);
        mensagemRecuperacao.textContent = "Não foi possível conectar. Tente novamente.";
        mensagemRecuperacao.classList.add("error");
    }

    mensagemRecuperacao.hidden = false;
    botaoRecuperacao.disabled = false;
});
