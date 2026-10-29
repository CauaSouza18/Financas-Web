const formularioRedefinicao = document.querySelector("#form-redefinicao");
const campoSenhaNova = document.querySelector("#password");
const campoConfirmacao = document.querySelector("#confirmar-password");
const mensagemRedefinicao = document.querySelector("#mensagem-redefinicao");
const linkLogin = document.querySelector("#link-login");
const botaoRedefinicao = document.querySelector("#botao-redefinicao");
const token = new URLSearchParams(window.location.hash.slice(1)).get("token");

function mostrarMensagem(texto, tipo = "") {
    mensagemRedefinicao.textContent = texto;
    mensagemRedefinicao.className = `feedback-message ${tipo}`.trim();
    mensagemRedefinicao.hidden = false;
}

async function validarLink() {
    if (!token) {
        mostrarMensagem("Este link está incompleto. Solicite uma nova recuperação de senha.", "error");
        return;
    }

    try {
        const resposta = await fetch("/api/redefinir-senha/validar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token })
        });
        const resultado = resposta.ok ? await resposta.json() : { valido: false };

        if (!resultado.valido) {
            mostrarMensagem("Este link expirou ou já foi usado. Solicite uma nova recuperação.", "error");
            return;
        }

        mensagemRedefinicao.hidden = true;
        formularioRedefinicao.hidden = false;
    } catch (erro) {
        console.error("Falha ao validar link de redefinição:", erro);
        mostrarMensagem("Não foi possível validar o link agora. Atualize a página e tente novamente.", "error");
    }
}

formularioRedefinicao.addEventListener("submit", async evento => {
    evento.preventDefault();
    mensagemRedefinicao.hidden = true;

    if (campoSenhaNova.value.length < 6) {
        mostrarMensagem("A senha deve ter pelo menos 6 caracteres.", "error");
        return;
    }
    if (campoSenhaNova.value !== campoConfirmacao.value) {
        mostrarMensagem("As senhas não coincidem.", "error");
        return;
    }

    botaoRedefinicao.disabled = true;
    try {
        const resposta = await fetch("/api/redefinir-senha", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ token, senha: campoSenhaNova.value, confirmarSenha: campoConfirmacao.value })
        });

        if (resposta.ok) {
            formularioRedefinicao.hidden = true;
            mostrarMensagem("Senha alterada. Agora você já pode entrar com a nova senha.", "success");
            linkLogin.hidden = false;
            return;
        }

        const erro = await resposta.json().catch(() => ({}));
        mostrarMensagem(erro.message || "O link expirou ou é inválido. Solicite uma nova recuperação.", "error");
    } catch (erro) {
        console.error("Falha ao redefinir senha:", erro);
        mostrarMensagem("Não foi possível conectar. Tente novamente.", "error");
    } finally {
        botaoRedefinicao.disabled = false;
    }
});

validarLink();
