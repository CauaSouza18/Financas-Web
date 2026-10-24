const formulario = document.querySelector("#form-cadastro");
const campoNome = document.querySelector("#nome");
const campoEmail = document.querySelector("#email");
const campoSenha = document.querySelector("#password");
const mensagemErro = document.querySelector("#erro-cadastro");

async function mensagemDeValidacao(resposta) {
    try {
        const erro = await resposta.json();
        return erro.message || erro.detail || "Dados inválidos.";
    } catch {
        return "Dados inválidos.";
    }
}

formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    mensagemErro.hidden = true;
    mensagemErro.textContent = "";

    const nome = campoNome.value;
    const email = campoEmail.value;
    const senha = campoSenha.value;

    try {
        const resposta = await fetch("/api/cadastro", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ nome, email, senha })
        });

        if (resposta.status === 201) {
            window.location.href = "/html/login.html";
            return;
        }

        if (resposta.status === 409) {
            mensagemErro.textContent = "E-mail já cadastrado";
        } else if (resposta.status === 400) {
            mensagemErro.textContent = await mensagemDeValidacao(resposta);
        } else {
            mensagemErro.textContent = "Não foi possível criar a conta. Tente novamente.";
        }
    } catch (erro) {
        console.error("Falha ao enviar cadastro:", erro);
        mensagemErro.textContent = "Não foi possível conectar. Tente novamente.";
    }

    mensagemErro.hidden = false;
});
