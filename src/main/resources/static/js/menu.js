const themeButton = document.getElementById("theme-toggle");
const themeLabel = document.getElementById("theme-label");
const formulario = document.querySelector("#form-ganho");
const filtroMes = document.querySelector("#filtro-mes");
const botaoSalvar = document.querySelector("#botao-salvar");
const botaoCancelar = document.querySelector("#botao-cancelar");
const linkSair = document.querySelector("#sair");
const tituloForm = document.querySelector("#titulo-form");
const descricaoForm = document.querySelector("#descricao-form");
const secaoForm = document.querySelector("#secao-form");
const campoData = document.querySelector("#data");
const tabela = document.querySelector("#lista-ganhos");

const DESCRICAO_PADRAO = descricaoForm.textContent;

function redirecionarSeNaoAutorizado(resposta) {
    if (resposta.status === 401) {
        window.location.href = "/html/login.html";
        return true;
    }
    return false;
}

// Logos usados junto aos nomes dos aplicativos.
const logoDoApp = {
    "99": { src: "../images/apps/99food.png", classe: "app-logo--99" },
    "iFood": { src: "../images/apps/ifood.png", classe: "app-logo--ifood" },
    "Uber Driver": { src: "../images/apps/uber.png", classe: "app-logo--uber" }
};

function criarIdentificacaoApp(plataforma) {
    const identificacao = document.createElement("span");
    identificacao.className = "app-name";

    const logoDoAppAtual = Object.prototype.hasOwnProperty.call(logoDoApp, plataforma)
        ? logoDoApp[plataforma]
        : null;
    if (logoDoAppAtual) {
        const logo = document.createElement("img");
        logo.className = `app-logo ${logoDoAppAtual.classe}`;
        logo.src = logoDoAppAtual.src;
        logo.alt = "";
        logo.setAttribute("aria-hidden", "true");
        logo.loading = "lazy";
        logo.decoding = "async";
        identificacao.append(logo);
    }

    const nome = document.createElement("span");
    nome.textContent = plataforma;
    identificacao.append(nome);
    return identificacao;
}

// Ganhos do mês exibido, para preencher o formulário ao editar
let ganhosDoMes = [];
let idEmEdicao = null;

const formatarMoeda = valor =>
    valor.toLocaleString("pt-BR", {style: "currency", currency: "BRL"});

// "2026-10-05" -> "05/10/2026". Não usa new Date() para não deslocar o dia pelo fuso horário.
const formatarData = dataIso => {
    const [ano, mes, dia] = dataIso.split("-");
    return `${dia}/${mes}/${ano}`;
};

const doisDigitos = numero => String(numero).padStart(2, "0");

// Mês atual pelo relógio local (toISOString usaria UTC e viraria o dia depois das 21h no Brasil)
function mesAtual() {
    const hoje = new Date();
    return `${hoje.getFullYear()}-${doisDigitos(hoje.getMonth() + 1)}`;
}

// Data local em "AAAA-MM-DD", N dias antes de hoje (0 = hoje, 1 = ontem)
function dataIsoDiasAtras(dias) {
    const data = new Date();
    data.setDate(data.getDate() - dias);
    return `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`;
}

const hojeIso = () => dataIsoDiasAtras(0);

const atalhosData = document.querySelectorAll("[data-dias-atras]");

function marcarAtalhoData() {
    atalhosData.forEach(botao => botao.classList.toggle("is-active",
        campoData.value === dataIsoDiasAtras(Number(botao.dataset.diasAtras))));
}

atalhosData.forEach(botao => botao.addEventListener("click", () => {
    campoData.value = dataIsoDiasAtras(Number(botao.dataset.diasAtras));
    marcarAtalhoData();
}));
campoData.addEventListener("input", marcarAtalhoData);

function periodoDoMes(anoMes) {
    const [ano, mes] = anoMes.split("-").map(Number);
    const ultimoDia = new Date(ano, mes, 0).getDate();
    return {
        inicio: `${anoMes}-01`,
        fim: `${anoMes}-${doisDigitos(ultimoDia)}`
    };
}

function marcarLinhaEmEdicao() {
    tabela.querySelectorAll("tr[data-id]").forEach(linha =>
        linha.classList.toggle("is-editing", Number(linha.dataset.id) === idEmEdicao));
}

function iniciarEdicao(ganho) {
    idEmEdicao = ganho.id;
    document.querySelector("#valor").value = Number(ganho.valor).toFixed(2);
    formulario.elements.plataforma.value = ganho.plataforma;
    campoData.value = ganho.data;
    marcarAtalhoData();
    tituloForm.textContent = "Editar ganho";
    descricaoForm.textContent =
        `Lançamento #${ganho.id} · ${ganho.plataforma} · ${formatarData(ganho.data)}`;
    botaoSalvar.textContent = "Atualizar ganho";
    botaoCancelar.hidden = false;
    secaoForm.classList.add("is-editing");
    marcarLinhaEmEdicao();
    formulario.scrollIntoView({ block: "center" });
    document.querySelector("#valor").focus({ preventScroll: true });
}

function encerrarEdicao() {
    idEmEdicao = null;
    formulario.reset();
    campoData.value = hojeIso();
    marcarAtalhoData();
    tituloForm.textContent = "Novo ganho";
    descricaoForm.textContent = DESCRICAO_PADRAO;
    botaoSalvar.textContent = "Salvar ganho";
    botaoCancelar.hidden = true;
    secaoForm.classList.remove("is-editing");
    marcarLinhaEmEdicao();
}

botaoCancelar.addEventListener("click", encerrarEdicao);

linkSair.addEventListener("click", async evento => {
    evento.preventDefault();

    try {
        await fetch("/api/logout", { method: "POST" });
    } catch (erro) {
        console.error("Falha ao encerrar sessão:", erro);
    }

    window.location.href = "/html/login.html";
});

formulario.addEventListener("submit", async evento => {
    evento.preventDefault();

    const ganho = {
        valor: Number(document.querySelector("#valor").value),
        plataforma: formulario.elements.plataforma.value,
        data: campoData.value
    };
    const editando = idEmEdicao !== null;

    try {
        const resposta = await fetch(editando ? `/api/ganhos/${idEmEdicao}` : "/api/ganhos", {
            method: editando ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ganho)
        });

        if (redirecionarSeNaoAutorizado(resposta)) return;

        if (!resposta.ok) {
            throw new Error("A API não conseguiu salvar o ganho.");
        }

        encerrarEdicao();
        // Mostra o mês do ganho salvo, para ele não "sumir" se a data for de outro mês
        filtroMes.value = ganho.data.slice(0, 7);
        await carregarGanhos();
    } catch (erro) {
        console.error(erro);
        alert("Não foi possível salvar. Confira o Console e o backend.");
    }
});

async function excluirGanho(ganho) {
    const confirmado = confirm(
        `Excluir o ganho de ${formatarMoeda(Number(ganho.valor))} (${ganho.plataforma}) de ${formatarData(ganho.data)}?`
    );
    if (!confirmado) return;

    try {
        const resposta = await fetch(`/api/ganhos/${ganho.id}`, { method: "DELETE" });
        if (redirecionarSeNaoAutorizado(resposta)) return;

        if (!resposta.ok) {
            throw new Error("A API não conseguiu excluir o ganho.");
        }
        if (idEmEdicao === ganho.id) encerrarEdicao();
        await carregarGanhos();
    } catch (erro) {
        console.error(erro);
        alert("Não foi possível excluir. Confira o Console e o backend.");
    }
}

tabela.addEventListener("click", evento => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;

    const ganho = ganhosDoMes.find(g => g.id === Number(botao.dataset.id));
    if (!ganho) return;

    if (botao.dataset.acao === "editar") iniciarEdicao(ganho);
    if (botao.dataset.acao === "excluir") excluirGanho(ganho);
});

filtroMes.addEventListener("change", () => {
    if (filtroMes.value) carregarGanhos();
});

async function carregarGanhos() {
    const { inicio, fim } = periodoDoMes(filtroMes.value);
    const resposta = await fetch(`/api/ganhos?inicio=${inicio}&fim=${fim}`);
    if (redirecionarSeNaoAutorizado(resposta)) return;

    if (!resposta.ok) {
        console.error("Falha ao carregar ganhos:", resposta.status);
        return;
    }
    const ganhos = await resposta.json();
    ganhosDoMes = ganhos;
    const total = ganhos.reduce((soma, ganho) => soma + Number(ganho.valor), 0);
    const somarPlataforma = plataforma =>
        ganhos
            .filter(ganho => ganho.plataforma === plataforma)
            .reduce((soma, ganho) => soma + Number(ganho.valor), 0);

    [["99", "99"], ["iFood", "ifood"], ["Uber Driver", "uber"]].forEach(([plataforma, chave]) => {
        const valor = somarPlataforma(plataforma);
        document.querySelector(`#total-${chave}`).textContent = formatarMoeda(valor);
        document.querySelector(`#parte-${chave}`).textContent =
            total > 0 ? `${Math.round(valor / total * 100)}%` : "";
    });
    document.querySelector("#total-geral").textContent = formatarMoeda(total);

    const quantidade = ganhos.length === 1 ? "1 lançamento" : `${ganhos.length} lançamentos`;
    document.querySelector("#quantidade-ganhos").textContent = ganhos.length === 0
        ? "Nenhum lançamento"
        : `${quantidade} · média de ${formatarMoeda(total / ganhos.length)}`;
    document.querySelector("#contagem-registros").textContent =
        ganhos.length === 1 ? "1 registro" : `${ganhos.length} registros`;
    tabela.innerHTML = "";

    if (ganhos.length === 0) {
        tabela.innerHTML = `<tr><td class="empty-row" colspan="5">Nenhum ganho registrado neste mês.</td></tr>`;
        return;
    }

    ganhos.forEach(ganho => {
        const linha = document.createElement("tr");
        linha.dataset.id = ganho.id;
        linha.classList.toggle("is-editing", ganho.id === idEmEdicao);

        linha.innerHTML = `
        <td>${formatarData(ganho.data)}</td>
        <td class="platform-cell"></td>
        <td class="record-id">#${ganho.id}</td>
        <td class="amount">${formatarMoeda(Number(ganho.valor))}</td>
        <td class="actions">
            <button class="row-button" type="button" data-acao="editar" data-id="${ganho.id}">Editar</button>
            <button class="row-button danger" type="button" data-acao="excluir" data-id="${ganho.id}">Excluir</button>
        </td>
    `;
        // append de texto para o nome do aplicativo não ser interpretado como HTML
        linha.querySelector(".platform-cell").append(criarIdentificacaoApp(ganho.plataforma));

        tabela.appendChild(linha);
    });
}

filtroMes.value = mesAtual();
campoData.value = hojeIso();
marcarAtalhoData();
carregarGanhos();

function setTheme(theme) {
    const dark = theme === "dark";
    document.documentElement.dataset.theme = dark ? "dark" : "light";
    themeButton.setAttribute("aria-pressed", String(dark));
    themeLabel.textContent = dark ? "Modo claro" : "Modo escuro";
    document.querySelector('meta[name="theme-color"]').content = dark ? "#101713" : "#f3f7f4";
}

setTheme(localStorage.getItem("financas-theme") || "light");
themeButton.addEventListener("click", () => {
    const nextTheme = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
    localStorage.setItem("financas-theme", nextTheme);
    setTheme(nextTheme);
});
