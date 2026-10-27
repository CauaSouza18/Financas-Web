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
const formularioDespesa = document.querySelector("#form-despesa");
const campoDataDespesa = document.querySelector("#data-despesa");
const botaoSalvarDespesa = document.querySelector("#botao-salvar-despesa");
const botaoCancelarDespesa = document.querySelector("#botao-cancelar-despesa");
const tituloFormDespesa = document.querySelector("#titulo-form-despesa");
const descricaoFormDespesa = document.querySelector("#descricao-form-despesa");
const secaoFormDespesa = document.querySelector("#secao-despesas");
const tabelaDespesas = document.querySelector("#lista-despesas");
const listaPlataformas = document.querySelector("#lista-plataformas");
const mensagemSemGanhos = document.querySelector("#mensagem-sem-ganhos");
const formularioRelatorio = document.querySelector("#form-relatorio");
const campoInicioRelatorio = document.querySelector("#relatorio-inicio");
const campoFimRelatorio = document.querySelector("#relatorio-fim");
const descricaoRelatorio = document.querySelector("#descricao-relatorio");
const totalRelatorioGanhos = document.querySelector("#total-relatorio-ganhos");
const totalRelatorioDespesas = document.querySelector("#total-relatorio-despesas");
const saldoRelatorio = document.querySelector("#saldo-relatorio");

const DESCRICAO_PADRAO = descricaoForm.textContent;
const DESCRICAO_PADRAO_DESPESA = descricaoFormDespesa.textContent;

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
    const nomeDaPlataforma = String(plataforma ?? "");
    const identificacao = document.createElement("span");
    identificacao.className = "app-name";

    const logoDoAppAtual = Object.prototype.hasOwnProperty.call(logoDoApp, nomeDaPlataforma)
        ? logoDoApp[nomeDaPlataforma]
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
    } else {
        const iconeGenerico = document.createElement("span");
        iconeGenerico.className = "app-logo app-logo--placeholder";
        iconeGenerico.setAttribute("aria-hidden", "true");
        iconeGenerico.textContent = nomeDaPlataforma.trim().slice(0, 1).toLocaleUpperCase("pt-BR");
        identificacao.append(iconeGenerico);
    }

    const nome = document.createElement("span");
    nome.textContent = nomeDaPlataforma;
    identificacao.append(nome);
    return identificacao;
}

// Ganhos do mês exibido, para preencher o formulário ao editar
let ganhosDoMes = [];
let despesasDoMes = [];
let idEmEdicao = null;
let idDespesaEmEdicao = null;

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

function marcarDespesaEmEdicao() {
    tabelaDespesas.querySelectorAll("tr[data-id]").forEach(linha =>
        linha.classList.toggle("is-editing", Number(linha.dataset.id) === idDespesaEmEdicao));
}

function iniciarEdicaoDespesa(despesa) {
    idDespesaEmEdicao = despesa.id;
    document.querySelector("#valor-despesa").value = Number(despesa.valor).toFixed(2);
    document.querySelector("#categoria").value = despesa.categoria;
    document.querySelector("#descricao-despesa").value = despesa.descricao || "";
    campoDataDespesa.value = despesa.data;
    tituloFormDespesa.textContent = "Editar despesa";
    descricaoFormDespesa.textContent = `Lançamento #${despesa.id} · ${despesa.categoria} · ${formatarData(despesa.data)}`;
    botaoSalvarDespesa.textContent = "Atualizar despesa";
    botaoCancelarDespesa.hidden = false;
    secaoFormDespesa.classList.add("is-editing");
    marcarDespesaEmEdicao();
    formularioDespesa.scrollIntoView({ block: "center" });
    document.querySelector("#valor-despesa").focus({ preventScroll: true });
}

function encerrarEdicaoDespesa() {
    idDespesaEmEdicao = null;
    formularioDespesa.reset();
    campoDataDespesa.value = hojeIso();
    tituloFormDespesa.textContent = "Nova despesa";
    descricaoFormDespesa.textContent = DESCRICAO_PADRAO_DESPESA;
    botaoSalvarDespesa.textContent = "Salvar despesa";
    botaoCancelarDespesa.hidden = true;
    secaoFormDespesa.classList.remove("is-editing");
    marcarDespesaEmEdicao();
}

botaoCancelarDespesa.addEventListener("click", encerrarEdicaoDespesa);

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
        await carregarLancamentos();
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
        await carregarLancamentos();
    } catch (erro) {
        console.error(erro);
        alert("Não foi possível excluir. Confira o Console e o backend.");
    }
}

formularioDespesa.addEventListener("submit", async evento => {
    evento.preventDefault();

    const despesa = {
        valor: Number(document.querySelector("#valor-despesa").value),
        categoria: document.querySelector("#categoria").value.trim(),
        descricao: document.querySelector("#descricao-despesa").value.trim(),
        data: campoDataDespesa.value
    };
    const editando = idDespesaEmEdicao !== null;

    try {
        const resposta = await fetch(editando ? `/api/despesas/${idDespesaEmEdicao}` : "/api/despesas", {
            method: editando ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(despesa)
        });
        if (redirecionarSeNaoAutorizado(resposta)) return;
        if (!resposta.ok) {
            throw new Error("A API não conseguiu salvar a despesa.");
        }

        encerrarEdicaoDespesa();
        filtroMes.value = despesa.data.slice(0, 7);
        await carregarLancamentos();
    } catch (erro) {
        console.error(erro);
        alert("Não foi possível salvar a despesa. Confira os dados e tente novamente.");
    }
});

async function excluirDespesa(despesa) {
    const confirmado = confirm(
        `Excluir a despesa de ${formatarMoeda(Number(despesa.valor))} (${despesa.categoria}) de ${formatarData(despesa.data)}?`
    );
    if (!confirmado) return;

    try {
        const resposta = await fetch(`/api/despesas/${despesa.id}`, { method: "DELETE" });
        if (redirecionarSeNaoAutorizado(resposta)) return;
        if (!resposta.ok) {
            throw new Error("A API não conseguiu excluir a despesa.");
        }
        if (idDespesaEmEdicao === despesa.id) encerrarEdicaoDespesa();
        await carregarLancamentos();
    } catch (erro) {
        console.error(erro);
        alert("Não foi possível excluir a despesa. Confira o Console.");
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

tabelaDespesas.addEventListener("click", evento => {
    const botao = evento.target.closest("button[data-acao]");
    if (!botao) return;

    const despesa = despesasDoMes.find(item => item.id === Number(botao.dataset.id));
    if (!despesa) return;

    if (botao.dataset.acao === "editar") iniciarEdicaoDespesa(despesa);
    if (botao.dataset.acao === "excluir") excluirDespesa(despesa);
});

filtroMes.addEventListener("change", () => {
    if (filtroMes.value) carregarLancamentos();
});

function renderizarQuebraRelatorio(lista, itens, total, montarNome) {
    itens.forEach(([chave, valorRecebido]) => {
        const valor = Number(valorRecebido);
        const porcentagem = total > 0 ? valor / total * 100 : 0;
        const linha = document.createElement("div");
        linha.className = "category-row";

        const cabecalho = document.createElement("div");
        cabecalho.className = "category-row-head";
        const nome = document.createElement("span");
        nome.className = "category-name";
        montarNome(nome, chave);

        const participacao = document.createElement("span");
        participacao.className = "share";
        participacao.textContent = `${Math.round(porcentagem)}%`;

        const valorFormatado = document.createElement("strong");
        valorFormatado.className = "category-value";
        valorFormatado.textContent = formatarMoeda(valor);
        cabecalho.append(nome, participacao, valorFormatado);

        const trilho = document.createElement("div");
        trilho.className = "category-track";
        trilho.setAttribute("role", "progressbar");
        trilho.setAttribute("aria-valuemin", "0");
        trilho.setAttribute("aria-valuemax", "100");
        trilho.setAttribute("aria-valuenow", String(porcentagem));
        trilho.setAttribute("aria-label", `${chave}: ${Math.round(porcentagem)}%`);
        const barra = document.createElement("span");
        barra.className = "category-bar";
        barra.style.width = `${porcentagem}%`;
        trilho.append(barra);

        linha.append(cabecalho, trilho);
        lista.append(linha);
    });
}

async function carregarRelatorio() {
    const inicio = campoInicioRelatorio.value;
    const fim = campoFimRelatorio.value;
    const listaCategorias = document.querySelector("#lista-categorias");
    const listaPlataformasRelatorio = document.querySelector("#lista-plataformas-relatorio");
    const mensagem = document.querySelector("#mensagem-relatorio");
    const mensagemSemDespesas = document.querySelector("#mensagem-sem-despesas");
    const mensagemSemGanhosRelatorio = document.querySelector("#mensagem-sem-ganhos-relatorio");

    listaCategorias.replaceChildren();
    listaPlataformasRelatorio.replaceChildren();
    mensagemSemDespesas.hidden = true;
    mensagemSemGanhosRelatorio.hidden = true;
    mensagem.classList.remove("is-error");
    mensagem.textContent = "Gerando relatório...";
    mensagem.hidden = false;
    totalRelatorioGanhos.textContent = "—";
    totalRelatorioDespesas.textContent = "—";
    saldoRelatorio.textContent = "—";
    saldoRelatorio.classList.remove("is-negative");

    if (!inicio || !fim || inicio > fim) {
        mensagem.textContent = "Informe um período válido: a data inicial deve ser anterior ou igual à data final.";
        mensagem.classList.add("is-error");
        return;
    }

    try {
        const parametros = `inicio=${encodeURIComponent(inicio)}&fim=${encodeURIComponent(fim)}`;
        const [respostaDespesas, respostaGanhos] = await Promise.all([
            fetch(`/api/despesas/por-categoria?${parametros}`),
            fetch(`/api/ganhos/por-plataforma?${parametros}`)
        ]);
        if (redirecionarSeNaoAutorizado(respostaDespesas) || redirecionarSeNaoAutorizado(respostaGanhos)) return;
        if (!respostaDespesas.ok || !respostaGanhos.ok) {
            throw new Error(`Falha nas APIs: despesas ${respostaDespesas.status}, ganhos ${respostaGanhos.status}.`);
        }

        const [dadosDespesas, dadosGanhos] = await Promise.all([
            respostaDespesas.json(),
            respostaGanhos.json()
        ]);
        const categorias = Object.entries(dadosDespesas).sort(([categoriaA, valorA], [categoriaB, valorB]) =>
            Number(valorB) - Number(valorA) || categoriaA.localeCompare(categoriaB, "pt-BR"));
        const plataformas = Object.entries(dadosGanhos).sort(([plataformaA, valorA], [plataformaB, valorB]) =>
            Number(valorB) - Number(valorA) || plataformaA.localeCompare(plataformaB, "pt-BR"));
        const totalDespesas = categorias.reduce((soma, [, valor]) => soma + Number(valor), 0);
        const totalGanhos = plataformas.reduce((soma, [, valor]) => soma + Number(valor), 0);
        const saldo = totalGanhos - totalDespesas;

        totalRelatorioGanhos.textContent = formatarMoeda(totalGanhos);
        totalRelatorioDespesas.textContent = formatarMoeda(totalDespesas);
        saldoRelatorio.textContent = formatarMoeda(saldo);
        saldoRelatorio.classList.toggle("is-negative", saldo < 0);
        descricaoRelatorio.textContent = `De ${formatarData(inicio)} a ${formatarData(fim)}`;

        renderizarQuebraRelatorio(listaCategorias, categorias, totalDespesas,
            (elemento, categoria) => { elemento.textContent = categoria; });
        renderizarQuebraRelatorio(listaPlataformasRelatorio, plataformas, totalGanhos,
            (elemento, plataforma) => { elemento.append(criarIdentificacaoApp(plataforma)); });
        mensagemSemDespesas.hidden = categorias.length > 0;
        mensagemSemGanhosRelatorio.hidden = plataformas.length > 0;
        mensagem.hidden = true;
    } catch (erro) {
        console.error("Falha ao gerar o relatório:", erro);
        mensagem.textContent = "Não foi possível gerar o relatório. Tente novamente.";
        mensagem.classList.add("is-error");
    }
}

formularioRelatorio.addEventListener("submit", evento => {
    evento.preventDefault();
    carregarRelatorio();
});

async function carregarTotaisPorPlataforma() {
    const { inicio, fim } = periodoDoMes(filtroMes.value);
    listaPlataformas.replaceChildren();
    mensagemSemGanhos.textContent = "Carregando ganhos por aplicativo...";
    mensagemSemGanhos.hidden = false;

    try {
        const resposta = await fetch(`/api/ganhos/por-plataforma?inicio=${inicio}&fim=${fim}`);
        if (redirecionarSeNaoAutorizado(resposta)) return;
        if (!resposta.ok) {
            console.error("Falha ao carregar ganhos por aplicativo:", resposta.status);
            mensagemSemGanhos.textContent = "Não foi possível carregar os ganhos por aplicativo.";
            mensagemSemGanhos.hidden = false;
            return;
        }

        const dados = await resposta.json();
        const plataformas = Object.entries(dados).sort(([plataformaA, valorA], [plataformaB, valorB]) =>
            Number(valorB) - Number(valorA) || plataformaA.localeCompare(plataformaB, "pt-BR"));
        if (plataformas.length === 0) {
            mensagemSemGanhos.textContent = "Sem ganhos neste mês";
            mensagemSemGanhos.hidden = false;
            return;
        }

        mensagemSemGanhos.hidden = true;
        const total = plataformas.reduce((soma, [, valor]) => soma + Number(valor), 0);
        plataformas.forEach(([plataforma, valorRecebido]) => {
            const valor = Number(valorRecebido);
            const porcentagem = total > 0 ? Math.round(valor / total * 100) : 0;
            const linha = document.createElement("div");
            linha.className = "platform-row";
            linha.append(criarIdentificacaoApp(String(plataforma ?? "")));

            const participacao = document.createElement("span");
            participacao.className = "share";
            participacao.textContent = `${porcentagem}%`;

            const valorFormatado = document.createElement("strong");
            valorFormatado.textContent = formatarMoeda(valor);
            linha.append(participacao, valorFormatado);
            listaPlataformas.append(linha);
        });
    } catch (erro) {
        console.error("Falha ao carregar ganhos por aplicativo:", erro);
        mensagemSemGanhos.textContent = "Não foi possível carregar os ganhos por aplicativo.";
        mensagemSemGanhos.hidden = false;
    }
}

async function carregarLancamentos() {
    const { inicio, fim } = periodoDoMes(filtroMes.value);
    let ganhos;
    let despesas;
    try {
        const respostaGanhos = await fetch(`/api/ganhos?inicio=${inicio}&fim=${fim}`);
        if (redirecionarSeNaoAutorizado(respostaGanhos)) return;
        if (!respostaGanhos.ok) {
            console.error("Falha ao carregar ganhos:", respostaGanhos.status);
            return;
        }
        ganhos = await respostaGanhos.json();

        const respostaDespesas = await fetch(`/api/despesas?inicio=${inicio}&fim=${fim}`);
        if (redirecionarSeNaoAutorizado(respostaDespesas)) return;
        if (!respostaDespesas.ok) {
            console.error("Falha ao carregar despesas:", respostaDespesas.status);
            return;
        }
        despesas = await respostaDespesas.json();
    } catch (erro) {
        console.error("Falha ao carregar os lançamentos:", erro);
        return;
    }

    ganhosDoMes = ganhos;
    despesasDoMes = despesas;
    const totalGanhos = ganhos.reduce((soma, ganho) => soma + Number(ganho.valor), 0);
    const totalDespesas = despesas.reduce((soma, despesa) => soma + Number(despesa.valor), 0);
    document.querySelector("#total-geral").textContent = formatarMoeda(totalGanhos - totalDespesas);
    document.querySelector("#resumo-saldo").textContent =
        `Ganhos ${formatarMoeda(totalGanhos)} · despesas ${formatarMoeda(totalDespesas)}`;

    document.querySelector("#contagem-registros").textContent =
        ganhos.length === 1 ? "1 registro" : `${ganhos.length} registros`;
    tabela.innerHTML = "";

    if (ganhos.length === 0) {
        tabela.innerHTML = `<tr><td class="empty-row" colspan="5">Nenhum ganho registrado neste mês.</td></tr>`;
    } else {
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
            // Insere o nome como texto para não interpretar conteúdo da API como HTML.
            linha.querySelector(".platform-cell").append(criarIdentificacaoApp(ganho.plataforma));

            tabela.appendChild(linha);
        });
    }

    document.querySelector("#contagem-despesas").textContent =
        despesas.length === 1 ? "1 registro" : `${despesas.length} registros`;
    tabelaDespesas.innerHTML = "";
    if (despesas.length === 0) {
        tabelaDespesas.innerHTML = `<tr><td class="empty-row" colspan="6">Nenhuma despesa registrada neste mês.</td></tr>`;
    } else {
        despesas.forEach(despesa => {
            const linha = document.createElement("tr");
            linha.dataset.id = despesa.id;
            linha.classList.toggle("is-editing", despesa.id === idDespesaEmEdicao);
            linha.innerHTML = `
                <td>${formatarData(despesa.data)}</td>
                <td class="expense-category"></td>
                <td class="expense-description"></td>
                <td class="record-id">#${despesa.id}</td>
                <td class="amount">${formatarMoeda(Number(despesa.valor))}</td>
                <td class="actions">
                    <button class="row-button" type="button" data-acao="editar" data-id="${despesa.id}">Editar</button>
                    <button class="row-button danger" type="button" data-acao="excluir" data-id="${despesa.id}">Excluir</button>
                </td>
            `;
            linha.querySelector(".expense-category").textContent = despesa.categoria;
            linha.querySelector(".expense-description").textContent = despesa.descricao || "—";
            tabelaDespesas.appendChild(linha);
        });
    }

    await Promise.all([carregarTotaisPorPlataforma(), carregarRelatorio()]);
}

filtroMes.value = mesAtual();
campoData.value = hojeIso();
campoDataDespesa.value = hojeIso();
const periodoInicialRelatorio = periodoDoMes(mesAtual());
campoInicioRelatorio.value = periodoInicialRelatorio.inicio;
campoFimRelatorio.value = periodoInicialRelatorio.fim;
marcarAtalhoData();
carregarLancamentos();

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
