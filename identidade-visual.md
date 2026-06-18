# Identidade Visual & Design System — EvoSales / ProspectAI

> **Documento de referência permanente.** Toda nova tela ou refatoração de UI deve seguir o que está aqui. Ao criar algo novo, pesquise o padrão das UIs de referência (Linear, Vercel, Stripe, Notion) e **adapte aos tokens deste sistema** — nunca introduza estilo que conflite com o guia.
>
> Fonte da verdade no código: [`src/index.css`](../src/index.css) (tokens `@theme` + `.dark`), [`tailwind.config.ts`](../tailwind.config.ts), componentes em [`src/components/ui/`](../src/components/ui/).
>
> 🔄 **Revisado em jun/2026** contra WCAG 2.2 AA, Vercel Web Interface Guidelines, *Refactoring UI* e boas práticas de design tokens. Mudanças relevantes e fontes em **§15**. Há uma **correção de contraste do `primary`** que exige ação — ver **§3.4**.

---

## 1. Princípios

1. **Minimalista e prático** — menos é mais. Hierarquia clara, espaçamento generoso, sem ruído visual.
2. **Sem sombras em conteúdo** — superfícies de conteúdo (cards, botões, inputs, stat cards) usam **borda + cor de superfície** para separação, nunca `shadow-*`. Sombra é reservada apenas a **overlays flutuantes** (ver §10).
3. **Foco em UX/UI** — o usuário precisa entender a tela em segundos. Ação primária óbvia; estados (carregando/vazio/erro) sempre tratados.
4. **Light & Dark first-class** — todo componente deve funcionar nos dois temas. Use tokens, nunca hex fixo inline.
5. **Consistência sobre criatividade** — reutilize componentes de [`src/components/ui/`](../src/components/ui/) e os padrões abaixo antes de criar algo novo.
6. **Pesquisar antes de criar** — para telas novas, busque o padrão consolidado nas UIs famosas e traga-o para os tokens do EvoSales.
7. **Tons de cinza primeiro** — projete a tela em escala de cinza e adicione cor por último. Isso força a hierarquia a nascer de **tamanho, peso e espaçamento**, e não da cor como muleta. Cor entra para reforçar uma hierarquia que já funciona em P&B (princípio do *Refactoring UI*).

### 1.1 Os 7 pilares de UI

Base conceitual do design system, segundo os [Princípios de UI Design da Figma](https://www.figma.com/pt-br/resource-library/principios-design-ui/). Toda tela deve respeitá-los.

| # | Pilar | O que é | Como aplicamos |
|---|-------|---------|----------------|
| 1 | **Hierarquia** | O usuário reconhece o que é principal à primeira vista | Três alavancas combinadas — **tamanho** (título `text-3xl/4xl` → body `text-sm` → label `text-xs`), **peso** (`font-medium`/`semibold` no que importa) e **cor** (`primary` só na ação principal; `text-muted` para o secundário). Uma ação primária óbvia por tela. |
| 2 | **Divulgação progressiva** | Dar a informação certa em cada etapa, sem sobrecarregar | Fluxos longos em etapas com indicador de progresso (cadastro, troca de senha, recuperação). Filtros avançados atrás de “Mais filtros” (popover), não tudo na tela. |
| 3 | **Consistência** | Mesmo elemento funciona igual em todo o produto | Reutilizar `src/components/ui/`; mesmas variantes de `Button`, status pills (§4), tokens. Desviar só com justificativa. |
| 4 | **Contraste** | Diferença visual guia a atenção | Ação crítica em destaque (`destructive`), secundárias discretas (`outline`/`ghost`). **Cor semântica ≠ peso visual**: para ações rotineiras, um botão secundário discreto costuma funcionar melhor que um vermelho gritante. Tokens garantem contraste em light/dark (ver §3.4). |
| 5 | **Acessibilidade** | Utilizável por todos (ver §1.2) | WCAG 2.2 AA, `focus-visible`, `aria-label`, `alt`, teclado (Radix), `prefers-reduced-motion`, alvos de toque ≥ 24px. |
| 6 | **Proximidade** | Itens relacionados ficam juntos; não-relacionados, separados | Agrupar por `space-y-*`/`gap-*`; ações afins na mesma linha; **isolar a ação destrutiva** dos demais botões para evitar clique acidental. |
| 7 | **Alinhamento** | Linhas/grid criam ordem e previsibilidade | `.main-content` centralizado (§8), grids responsivos, ação primária sempre à direita (§7), labels alinhados. |

### 1.2 Acessibilidade — WCAG 2.2 AA (pilar 5, detalhado)

Nosso alvo é **WCAG 2.2 nível AA** (padrão W3C atual, equivalente à ISO/IEC 40500:2025). Os itens abaixo são o “o quê/por quê”; o “como” em código está na §12.

**Fundamentos (já valiam em 2.1):**

- **Contraste:** mínimo AA — **4.5:1** para texto normal, **3:1** para texto grande (≥ 24px, ou ≥ 18.66px bold) e para **componentes de UI / objetos gráficos** (bordas de input, ícones que carregam significado). Usar os tokens (calibrados para light/dark) em vez de hex avulso. **Validar §3.4.**
- **Foco visível:** todo elemento interativo tem `focus-visible` (ring de 3px no `ring`/primary) — nunca remover o outline sem substituto. Prefira `:focus-visible` a `:focus`.
- **Rótulos:** botão só com ícone precisa de `aria-label` (ou `title`); imagem precisa de `alt` (`alt=""` se decorativa); ícone decorativo recebe `aria-hidden="true"`; texto só para leitor de tela via `sr-only`.
- **Teclado:** componentes Radix já entregam navegação/ARIA — prefira-os a recriar do zero. Modais fecham no `Esc`, com foco preso enquanto abertos. Use HTML semântico (`<button>`, `<a>`, `<label>`, `<table>`) antes de ARIA.
- **Movimento:** respeitar `prefers-reduced-motion` (regra global em [`src/index.css`](../src/index.css)); nada essencial pode depender só de animação.

**Critérios novos da WCAG 2.2 que afetam diretamente este produto:**

- **Tamanho do alvo — mínimo 24×24 px (2.5.8, AA):** todo alvo de toque/clique tem pelo menos 24px. Se o ícone é menor, **expanda a área de clique** (padding/pseudo-elemento) até ≥ 24px. Em mobile, mire 44px. Aplica-se a ícones de ação, paginação, chips removíveis, “fechar” de toasts/modais.
- **Foco não obscurecido (2.4.11, AA):** com **header sticky** (§8), garanta que o elemento focado não fique escondido atrás dele — use `scroll-margin-top` nas âncoras e teste o tab por toda a tela.
- **Alternativas a arrastar (2.5.7, AA):** qualquer ação por *drag-and-drop* (ex.: reordenar etapas/colunas de funil) precisa de alternativa por clique/botão (mover acima/abaixo, menu).
- **Autenticação acessível (3.3.8, AA):** login/cadastro/recuperação **não** podem exigir teste cognitivo. **Nunca bloquear colar** em campos de senha/código e **não impedir gerenciadores de senha**. OTP deve aceitar colar o código inteiro.
- **Ajuda consistente (3.2.6, A)** e **entrada redundante (3.3.7, A):** mecanismos de ajuda (suporte/contato) ficam no mesmo lugar entre telas; não peça ao usuário reinformar dados já fornecidos no mesmo fluxo (pré-preencher/“usar dados anteriores”).

**Estrutura de página acessível:**

- Hierarquia de headings `<h1>`–`<h6>` sem pular níveis; incluir **skip link** (“Pular para o conteúdo”) apontando para o `<main>` do [`Layout`](../src/components/Layout/index.tsx).
- Atualizações assíncronas (toasts, validação, resultados de busca) anunciadas via **`aria-live="polite"`** (use `assertive` apenas para erros que exigem ação imediata).

---

## 2. Stack

| Item | Tecnologia |
|------|-----------|
| CSS | Tailwind CSS v4 (tokens via `@theme` em [`src/index.css`](../src/index.css)) |
| Componentes | shadcn/ui + Radix UI ([`src/components/ui/`](../src/components/ui/)) |
| Tema | `next-themes` — `attribute="class"`, `storageKey="theme"`, default `light` ([`src/main.tsx`](../src/main.tsx)) |
| Toggle de tema | [`src/pages/settings/components/ThemeSection.tsx`](../src/pages/settings/components/ThemeSection.tsx) |
| Toasts | `sonner` + wrapper [`src/utils/toast.tsx`](../src/utils/toast.tsx) |
| Ícones | `lucide-react` |
| Animação | `motion` (Framer Motion) |
| Formulários | `react-hook-form` (+ validação) |
| Estado na URL | `useSearchParams` do **React Router v6** (não `nuqs`/Next — ver §12.2) |
| Variantes | `class-variance-authority` + `tailwind-merge` (`cn`) |

> Persistência: **token/sessão sempre em cookie**; `localStorage` só para preferências do cliente (ex.: tema).

---

## 3. Paleta de Cores

### 3.0 Camadas de token (como o sistema se organiza)

Seguimos o modelo de **três camadas** comum a design systems maduros (ex.: Carbon, da IBM). Nomeie pela **intenção**, não pela aparência (`primary`, `destructive` — nunca `teal`, `red`).

1. **Primitivos** — valores crus (hex, escalas Tailwind como `violet-500`). Não carregam significado; só existem para alimentar a camada semântica.
2. **Semânticos** — mapeiam primitivos a papéis: `primary`, `success`, `destructive`, `background`, `text`… São o que você usa no dia a dia.
3. **De componente** — decisões locais de um componente (ex.: tokens `--sidebar-*`). Só criar quando a camada semântica não der conta.

**Por que existem dois conjuntos no CSS** (e não se preocupe, eles convivem de propósito):

- **`--color-*` (§3.1)** — camada semântica consumida pelo **seu código de app** via utilities Tailwind: `bg-primary`, `text-text-muted`, `border-border`.
- **`--background`/`--card`/`--primary` em HSL (§3.2)** — a **mesma camada semântica** no formato que os **componentes shadcn/ui** consomem internamente (`hsl(var(--card))`).

> **Regra de uso:** no código de aplicação, use as utilities `--color-*` (`bg-primary`, `text-text`); deixe os componentes de `ui/` usarem seus tokens HSL por dentro. **Os dois conjuntos são mantidos em sincronia em [`src/index.css`](../src/index.css)** — se mudar uma cor, atualize os dois. Nunca cravar hex inline.

### 3.1 Cores semânticas (tokens `--color-*`, usados como `bg-primary`, `text-text`, etc.)

| Token Tailwind | Uso | **Light** | **Dark** |
|----------------|-----|-----------|----------|
| `primary` | Acento/destaque/ativo (texto, borda, ring, dot, track, underline, soft-bg) | `#00c9b7` (teal) | `#60a5fa` (blue-400) |
| `primary-hover` | Hover do primary | `#00b5a5` | `#3b82f6` |
| `primary-light` | Fundo suave do primary (texto escuro por cima) | `#e6faf8` | `#1c2035` |
| `primary-strong` | Preenchimento que carrega **texto/ícone branco** (botões, badges, dia selecionado de calendário, `.gradient-primary`) | `#0a8074` (≈ 4.8:1 c/ branco) | `#1e40af` (blue-800) |
| `primary-strong-hover` | Hover do `primary-strong` | `#097064` | `#1e3a8a` |
| `on-primary` | Cor de texto/ícone **sobre** o `primary-strong` | `#ffffff` | `#ffffff` |
| `secondary` | Ação/destaque secundário | `#2563eb` (azul) | `#2563eb` (azul — mesmo valor nos dois temas) |
| `success` | Sucesso, positivo (texto/borda/soft-bg) | `#10b981` | `#10b981` |
| `success-strong` | Preenchimento `success` com **texto branco** (botão Ganho) — ≈ 5:1 c/ branco | `#047857` | `#047857` |
| `destructive` | Erro, destrutivo (texto/borda/soft-bg) | `#ef4444` | `#ef4444` |
| `destructive-strong` | Preenchimento `destructive` com **texto branco** (botão Perda/Excluir) — ≈ 5.9:1 c/ branco | `#b91c1c` | `#b91c1c` |
| `warning` | Aviso, atenção | `#f59e0b` | `#f59e0b` |
| `info` | Informação | `#3b82f6` | `#3b82f6` |
| `background` | Fundo de superfície (cards) | `#ffffff` | `#151929` |
| `background-body` | Fundo da página | `#fcfcfc` | `#0f1220` |
| `text` | Texto principal | `#1e293b` | `#f6f7fb` |
| `text-muted` | Texto secundário / placeholder | `#64748b` | `#b8bfd7` |
| `border` | Bordas e divisores | `#e2e8f0` | `#1e2438` |
| `surface-hover` | Hover de superfícies | `#f1f5f9` | `#1c2035` |
| `input-background` | Fundo de inputs | `#ffffff` | `#151929` |

> ⚠️ **O `primary` muda de cor entre temas** — teal no light, **azul no dark** (blue-400 acento / blue-800 fill). É **intencional**: o teal não tem contraste adequado sobre fundo escuro. O `secondary` é **azul** (`#2563eb`, blue-600) nos dois temas — o roxo `#5549df` foi **aposentado** (jun/2026); escolhido distinto do `primary` e do `info` para não colidir. Sempre use os tokens (`primary`/`primary-strong`/`secondary`); nunca cravar hex.

### 3.2 Tokens HSL (shadcn — usados como `hsl(var(--card))`)

| Token | Light | Dark |
|-------|-------|------|
| `--background` | `210 20% 98%` | `230 32% 11%` |
| `--foreground` | `220 25% 10%` | `230 60% 98%` |
| `--card` | `0 0% 100%` | `230 30% 12%` |
| `--popover` | `0 0% 100%` | `230 30% 12%` |
| `--primary` | `168 76% 42%` | `213 94% 68%` |
| `--primary-foreground` | `0 0% 100%` | `0 0% 100%` |
| `--primary-strong` | `174 85% 27%` | `226 71% 40%` |
| `--secondary` | `221 83% 53%` | `221 83% 53%` |
| `--muted` | `210 20% 96%` | `230 28% 14%` |
| `--muted-foreground` | `220 15% 50%` | `224 25% 72%` |
| `--accent` | `168 76% 42%` | `230 30% 16%` |
| `--destructive` | `0 84.2% 60.2%` | `0 84% 60%` |
| `--border` / `--input` | `220 15% 90%` | `230 25% 16%` |
| `--ring` | `168 76% 42%` | `213 94% 68%` |
| `--nav-bg` | `0 0% 100%` | `230 30% 12%` |

A sidebar tem tokens próprios (`--sidebar-*`) — ver [`src/index.css`](../src/index.css).

### 3.3 Regra de ouro

```tsx
// ✅ Correto — token, funciona nos dois temas
<div className="bg-primary-strong text-on-primary" />   {/* preenchimento com texto branco (§3.4) */}
<div className="text-text-muted border-border" />

// ❌ Evitar — hex fixo / cor inline / style condicional por tema
<div style={{ color: theme === "dark" ? "#fff" : "#64748b" }} />
<div className="bg-[#00c9b7]" />
```

### 3.4 Contraste do `primary` — correção obrigatória ⚠️

O **teal do tema light não passa em AA quando recebe texto/ícone branco**. Medições (sRGB, fórmula WCAG):

| Combinação | Contraste | Texto AA (4.5) | Grande/UI AA (3) |
|-----------|-----------|:---:|:---:|
| `#00c9b7` (teal) + texto **branco** | **2.09:1** | ❌ | ❌ |
| `#00b5a5` (`primary-hover`) + branco | **2.58:1** | ❌ | ❌ |
| `#00c9b7` (teal) + texto **escuro** `#1e293b` | **6.99:1** | ✅ | ✅ |
| `#2563eb` (azul, `secondary`) + branco | **≈ 5.1:1** | ✅ | ✅ |

Ou seja: o exemplo `bg-primary text-white` que constava como “correto” **reprova** no light. O azul do `secondary` passa.

> ✅ **Implementado (jun/2026) — Opção A.** Mantemos o teal `#00c9b7` para **acentos, bordas, fundos `primary-light` (com texto escuro), estados ativos, tracks de toggle e underline de abas**; e usamos `primary-strong` = **`#0a8074`** (≈ 4.8:1 com branco) como **fundo de botões/badges/checkbox-check/dia-selecionado e do `.gradient-primary`** que carregam texto/ícone branco (`on-primary` = `#ffffff`). Atenção: o `#0a8f82` sugerido originalmente só atinge ≈ 4.0:1 e **reprova** — por isso a calibração para `#0a8074`. No dark, `primary-strong` resolve para o azul escuro `#1e40af`, que já passa.

Para referência, a alternativa não adotada:

- **Opção B — texto escuro sobre o teal:** manter `#00c9b7` como fundo e usar **texto `#1e293b`** por cima (`on-primary` escuro no light). Resolve sem novo tom, mas muda a aparência do botão primário.

**Regra permanente:** qualquer **texto ou ícone significativo sobre superfície colorida** deve atingir **≥ 4.5:1** (texto) / **≥ 3:1** (texto grande e componentes de UI). Validar em um conferidor de contraste antes de subir. Vale também para a utility `.gradient-primary` (§6.1) e para badges/pills.

---

## 4. Cores de Status

Status nunca usam `primary`/`secondary` — têm paleta semântica própria. **Acessibilidade:** cor **não pode ser o único** indicador — sempre pareie com rótulo de texto e/ou bolinha (`dot`), o que já fazemos nas pills.

### 4.1 Status de Lead — [`LeadStatusPill.tsx`](../src/pages/chat/components/LeadStatusPill.tsx)

`LEAD_STATUS_META` é a **fonte única** (chip, filtro, badge da tela de Leads). Pill arredondado (`rounded-full`) com bolinha (`dot`):

| Status | Label | Cor base | Fundo / Texto / Dot |
|--------|-------|----------|---------------------|
| `START` | Novo | violet | `bg-violet-100 dark:bg-violet-500/10` · `text-violet-900 dark:text-violet-400` · `bg-violet-500` |
| `PROGRESS` | Em Progresso | sky | `bg-sky-100 dark:bg-sky-500/10` · `text-sky-900 dark:text-sky-400` · `bg-sky-500` |
| `WIN` | Ganho | teal | `bg-teal-100 dark:bg-teal-500/10` · `text-teal-900 dark:text-teal-400` · `bg-teal-500` |
| `LOST` | Perdido | rose | `bg-rose-100 dark:bg-rose-500/10` · `text-rose-900 dark:text-rose-400` · `bg-rose-500` |
| `STANDBY` | Em Espera | orange | `bg-orange-100 dark:bg-orange-500/10` · `text-orange-900 dark:text-orange-400` · `bg-orange-500` |
| `IN_QUEUE` | Na Fila | yellow | `bg-yellow-100 dark:bg-yellow-500/10` · `text-yellow-900 dark:text-yellow-400` · `bg-yellow-500` |
| `STOPPED` | Parado | slate | `bg-slate-100 dark:bg-slate-500/10` · `text-slate-900 dark:text-slate-400` · `bg-slate-500` |

### 4.2 Tones de Atividade / Job — [`jobActivityStatus.tsx`](../src/components/ui/jobActivityStatus.tsx)

`TONE_CLASSES` — paleta semântica reutilizável (badge `ActivityStatusBadge`). Cada tone tem fundo+texto+borda em light e dark:

| Tone | Cor | Quando usar |
|------|-----|-------------|
| `success` | green | Concluído com êxito (Concluída, Lida, Entregue, Enviado) |
| `info` | blue | Em andamento / neutro-positivo (Em execução, Enviada, Enviando) |
| `warning` | amber | Agendado / na fila (PENDING, QUEUED) |
| `blocked` | orange | Bloqueado (template não aprovado, saldo insuficiente, etc.) |
| `danger` | red | Falha (Falhou, Não atendida, Falha na entrega) |
| `neutral` | gray | Realizado / detalhe indisponível |

A JobActivity tem **dois eixos**: execução (step badge, *subdued*) e resultado (chip com a cor forte). Não misturar — ver doc da rota.

### 4.3 Funil de Leads (Dashboard) — [`useLeadsFunnel.ts`](../src/pages/dashboard/hooks/useLeadsFunnel.ts)

| Categoria | Status agrupados | Cor |
|-----------|------------------|-----|
| Novo | `START` | `secondary` (azul `#2563eb`) |
| Em processo | `IN_QUEUE`, `STANDBY`, `PROGRESS` | `#0ea5e9` (sky) — token `--color-status-process` |
| Ganho | `WIN` | `primary` (teal `#00c9b7` light / azul `#60a5fa` dark) |
| Perdido | `LOST` | `#f43f5e` (rose) — token `--color-status-lost` |

> Os gráficos do dashboard (`CallsPerDayChart`, `LeadsConversionsChart`, `useLeadsFunnel`) consomem cores via `var(--color-…)` — **nunca** hex cravado. **Ganho** (funil) e **Convertidos** (Leads & Conversões) usam o **`primary`** e **Novo / Leads** usa o **`secondary`** — ambos acompanham o tema (light/dark). As demais cores são **fixas nos dois temas** (não acompanham o tema), em [`src/index.css`](../src/index.css): `--color-status-won` (`#14b8a6`, teal) usado em **"Atendidas"** (Ligações por Dia), `--color-status-process` (`#0ea5e9`, sky) em Em processo, e `--color-status-lost` (`#f43f5e`, rose) em Perdido/Perdidas.

---

## 5. Toasts — [`custom-toast.tsx`](../src/components/ui/custom-toast.tsx) + [`toast.tsx`](../src/utils/toast.tsx)

API: `toast.success() · toast.error() · toast.warning() · toast.info() · toast.notification() · toast.message()`. Duração padrão **5s** com **pausa no hover** (sem barra de progresso — visual minimalista). Posição: **canto inferior direito** (`<Toaster position="bottom-right" />`). Aceita `description`.

| Variante | Ícone | Cor do ícone | `aria-live` |
|----------|-------|---------------------|-------------|
| `success` | CheckCircle2 | `#10b981` | polite |
| `error` | AlertCircle | `destructive` (`#ef4444`) | assertive |
| `warning` | AlertTriangle | `#f59e0b` | polite |
| `info` | Info | `#3b82f6` | polite |
| `notification` | Bell | `primary` (não fecha ao clicar no corpo) | polite |
| `default` | Info | `text-muted` | polite |

```tsx
toast.success("Usuário atualizado com sucesso!");
toast.error("Não foi possível salvar", { description: "Verifique a conexão e tente de novo." });
```

- O toast é um overlay → **mantém** elevação leve (`shadow-lg`, exceção da regra de sombras, ver §10). Layout: disco do ícone (fundo `cor/10` + ícone colorido) · título `text-sm font-semibold` · subtítulo `text-xs text-text-muted` · botão fechar.
- O `sonner` já expõe a região com `aria-live`; mensagens devem **incluir o próximo passo**, não só o problema (ver §12 — Conteúdo & copy).
- Toast **não substitui** erro de formulário: erros de campo vão **inline** ao lado do campo (§12.1).

---

## 6. Componentes

### 6.1 Button — [`button.tsx`](../src/components/ui/button.tsx)

**Variantes:** `default` (primary), `secondary`, `outline`, `ghost`, `destructive`, `link`.
**Tamanhos:** `sm` (h-8), `default` (h-9), `lg` (h-10), `icon` (size-9), `icon-sm` (size-8), `icon-lg` (size-10).
**Radius:** `rounded-md`. **Sem sombra.** Disabled: `opacity-70 cursor-not-allowed`.

```tsx
<Button>Salvar</Button>                          {/* ação primária */}
<Button variant="outline">Cancelar</Button>       {/* secundária */}
<Button variant="destructive">Excluir</Button>     {/* destrutiva */}
<Button variant="ghost" size="icon-sm">…</Button>  {/* ação discreta */}
```

- **Alvo de toque ≥ 24px** (44px em mobile, §1.2). Os tamanhos `icon-sm`/`sm` (32px) já atendem; se criar algo menor, expanda a área de clique.
- **Estados interativos aumentam o contraste** — hover/active/focus devem ser mais salientes que o repouso (feedback visível).
- Botão só com ícone exige `aria-label` (§1.2).
- Para destaque extra existe a utility `.gradient-primary` (gradiente entre os tons `primary-strong`, calibrado para texto/ícone branco passar em AA — §3.4). Use com parcimônia, só em CTAs principais.
- **Não** use tamanhos custom (`h-12`, etc.) — escolha `sm`/`default`/`lg`.

### 6.2 Badge — [`badge.tsx`](../src/components/ui/badge.tsx)

Variantes `default`/`secondary`/`destructive`/`outline`. `rounded-md`, `text-xs`, `px-2 py-0.5`. Para status use as paletas da §4, não as variantes cruas.

### 6.3 Card — [`card.tsx`](../src/components/ui/card.tsx)

`bg-background border border-border rounded-md`. **Sem sombra** — separação por borda. Sub-componentes: `CardHeader` / `CardTitle` (h4) / `CardDescription` (`text-text-muted`) / `CardContent` / `CardFooter`.

### 6.4 Input

`h-9`, `rounded-md`, `border-input`, `bg-input-background`. Focus: ring de 3px no `ring`/primary. Erro: ring/borda `destructive` + mensagem inline (§12.1). Placeholder: `text-text-muted` com `opacity: 0.6` global (regra `input::placeholder, textarea::placeholder` em [`src/index.css`](../src/index.css)) — não usar cor de placeholder mais escura que `text-text-muted`.

- **`type` e `inputmode` corretos** (`email`, `tel`, `url`, `number`) e `autocomplete` significativo.
- **Mobile:** `font-size` ≥ **16px** no input para evitar o auto-zoom do iOS Safari ao focar (ou garantir o `viewport` adequado).
- **Erro do campo** ligado ao input por `aria-describedby`; campo inválido marcado com `aria-invalid`.
- **Nunca** bloquear colar (`onPaste` + `preventDefault`).

### 6.5 StatCard / agent-card

Cards de métrica e de agente usam **borda + hover sutil de borda/translate**, sem sombra de conteúdo. Números em `tabular-nums` (§9, §12.3).

---

## 7. Posicionamento de Botões

**Padrão dominante e obrigatório:** ação primária à **direita**, cancelar à **esquerda**.

### 7.1 Modais / diálogos

Use `DialogFooter` (ou `AlertDialogFooter`), que já aplica:

```
flex flex-col-reverse gap-2 sm:flex-row sm:justify-end
```

- **Desktop:** botões à direita, ordem `Cancelar` → `Ação primária`.
- **Mobile:** empilhados, **ação primária por cima** (col-reverse).

```tsx
<DialogFooter>
  <Button variant="outline" onClick={onClose}>Cancelar</Button>
  <Button onClick={onSave}>Salvar</Button>
</DialogFooter>
```

> Nunca usar `flex gap-2` cru no rodapé de modal — quebra o comportamento responsivo.

### 7.2 Header de página

Título à esquerda, **ação primária à direita** via `HeaderSection` ([`header-section.tsx`](../src/components/ui/header-section.tsx)):

```tsx
<HeaderSection action={<Button>Novo usuário</Button>}>
  <HeaderSectionTitle>Usuários</HeaderSectionTitle>
  <HeaderSectionDescription>Gerencie os acessos.</HeaderSectionDescription>
</HeaderSection>
```

---

## 8. Telas & Layout

Layout base: [`src/components/Layout/index.tsx`](../src/components/Layout/index.tsx).

- **Skip link** (“Pular para o conteúdo”) como primeiro foco da página, levando ao `<main>`.
- **Header sticky** no topo (`--nav-bg`, `border-b border-border`). Com sticky, usar `scroll-margin-top` nas âncoras para não obscurecer o foco (§1.2).
- **`.main-content`**: `max-width: 1280px`, centralizado, padding responsivo (1rem mobile → 2rem → 2.5rem). Variante `--chat`: `max-width: 1520px`.
- **Bottom nav** em mobile.
- Evitar scrollbars indesejadas: `overflow-x-hidden` em containers e corrigir o overflow na origem.

**Estrutura de página padrão:**

```tsx
<div className="space-y-6">
  <HeaderSection action={…}>…</HeaderSection>
  {/* filtros (estado na URL — §12.2) */}
  {/* conteúdo: tabela, grid de cards, etc. */}
</div>
```

- Espaçamento entre seções: `space-y-6`. Entre cards: `gap-4`/`gap-6`.
- **Empty state** (componente dedicado por tela): círculo com ícone + título + mensagem **contextual** (muda se há filtro ativo). Trate a tela vazia como um **convite à ação**, não como um vazio morto.
- **Loading:** `Skeleton` (`bg-accent animate-pulse rounded-md`) ou spinner `border-primary border-t-transparent animate-spin` / `<Loader2 className="animate-spin" />`.
- **Erro:** explique **o que aconteceu e como resolver**, na voz do produto (sem pedir desculpas, sem mensagem vaga). Inclua a próxima ação (tentar de novo, contatar suporte).

---

## 9. Tokens de Design

| Categoria | Valores |
|-----------|---------|
| **Border-radius** | `rounded-md` = `0.5rem` (**dominante**) · `lg` = `0.75rem` (cards/popovers maiores) · `full` (pills/avatares) · `sm` = `0.375rem` |
| **Espaçamento** | escala restrita, base `0.25rem` (4px). Comuns: `p-2`/`p-4`/`p-6`, `gap-4`/`gap-6`, `space-y-6`. Não invente valores fora da escala. |
| **Tipografia** | Inter (300–700). Título de página `text-3xl lg:text-4xl font-medium tracking-tight` (`text-balance`). Body `text-sm`/`text-base`. Badges `text-xs` |
| **Título de popover/modal** | `text-sm font-semibold text-text` (header de popover; `DialogTitle` usa `text-lg font-semibold`) |
| **Label de seção** | `text-[12px] font-medium uppercase tracking-wide text-text-muted` — rótulos de grupos em popovers/modais/filtros (ex.: “OUTROS STATUS”, “RESPONSÁVEL”). Labels são secundários: menores, mais leves, em maiúsculas pequenas. |
| **Números** | `tabular-nums` em tabelas, `StatCard`, eixos/tooltips de gráfico (§12.3) |
| **Breakpoints** | `sm` 640px · `md` 768px · `lg` 1024px (mobile-first) |
| **Z-index** | header `z-50` · overlays acima · conteúdo base |

---

## 10. Sombras (diretriz)

**Sombras não fazem parte do padrão visual de conteúdo.** UIs modernas (Linear, Vercel) separam superfícies por **borda e cor** — é o que seguimos.

- ❌ **Sem `shadow-*`** em: cards, botões, inputs, stat cards, tabelas, seções.
- ✅ **Elevação mínima permitida apenas em overlays flutuantes** que se sobrepõem ao conteúdo: `dropdown-menu`, `context-menu`, `select`, `popover`, `hover-card`, `dialog`/`alert-dialog`, `sheet`, `toast`, tooltip de gráfico.
- Os componentes [`dropdown-menu.tsx`](../src/components/ui/dropdown-menu.tsx) e [`popover.tsx`](../src/components/ui/popover.tsx) aplicam **`shadow-md` por padrão** (`DropdownMenuContent`/`DropdownMenuSubContent` e `PopoverContent`), em conjunto com `border border-border`. Vale para todos os dropdowns e popovers (menu do usuário, notificações, card de créditos, etc.).

Ao criar tela nova: separe blocos com `border border-border` e variação de `background`/`surface-hover`, **nunca** com sombra.

---

## 11. Animações

`motion` (Framer Motion) para microinterações; transições CSS para hover/tema.

**Padrão a manter (sutil, funcional):**

| Padrão | Valores | Onde |
|--------|---------|------|
| Fade-in | `opacity 0→1` | entrada de conteúdo |
| Slide-in | `y:20→0` ou `x:20→0`, `~0.3–0.4s` | cards, steps |
| Stagger | `delay: index * 0.05` | listas/grids |
| Spring (tabs) | `stiffness 380, damping 32` | indicador de aba ativo |
| Easing padrão | `cubic-bezier(0.4, 0, 0.2, 1)` | geral |
| Transição de tema | `0.3s` cores (global) | troca light/dark |

**Regras técnicas (compositor-friendly):**

- Anime **apenas `transform` e `opacity`** — são baratos para o navegador.
- **Nunca `transition: all`** — liste as propriedades explicitamente.
- Defina `transform-origin` correto. Em SVG, aplique transform no `<g>` com `transform-box: fill-box; transform-origin: center`.
- Animações devem ser **interrompíveis** — responder a input do usuário no meio.

**Reduzir / evitar:** loops decorativos pesados (flutuantes infinitos, *scale-pulse*) — peso visual sem ganho de UX.

**Acessibilidade:** regra global em [`src/index.css`](../src/index.css) respeita `prefers-reduced-motion` (zera durações) — nada essencial pode depender só de movimento.

**Como desabilitar uma animação (diretriz do projeto):** **neutralizar** zerando `duration`/`initial` no `motion`, **sem apagar o JSX** — assim ela pode ser reativada depois. Não remover o componente.

```tsx
// Neutralizado (reativável), em vez de remover:
<motion.div initial={false} transition={{ duration: 0 }} … />
```

---

## 12. Boas práticas adicionais (Web Interface Guidelines, adaptadas)

> Práticas das [Web Interface Guidelines](https://github.com/vercel-labs/web-interface-guidelines) **adaptadas à nossa stack** (SPA Vite + **React 18** + **React Router v6**). Complementam — não substituem — as seções acima. Itens de Next.js/RSC/React 19 (hidratação de SSR, `nuqs`, `priority` do `next/image`) ficam de fora; os equivalentes universais estão abaixo.

### 12.1 Formulários (complementa §6.4)

Usamos **React Hook Form**. Para todo formulário:

- **Erros inline** ao lado do campo (cor `destructive`), nunca só um toast genérico; no submit, **focar o primeiro campo com erro**. Ligue a mensagem ao input com `aria-describedby` + `aria-invalid`.
- **Submit habilitado** até o request começar; durante o envio, desabilitar e mostrar spinner (`<Loader2 className="animate-spin" />`) — não desabilitar de forma especulativa.
- **`type`/`inputmode` corretos** e `autocomplete` significativo; `spellCheck={false}` em e-mail/código/usuário; `autocomplete="off"` em campos que não são de autenticação, para não disparar o gerenciador de senhas.
- **Label sempre clicável** (`htmlFor` ou envolvendo o controle); checkbox/radio com alvo de clique único (sem zonas mortas).
- **Placeholders** mostram um exemplo e terminam com `…` (ex.: `"nome@empresa.com"`).
- **Nunca** bloquear colar — vale especialmente para senha/OTP (§1.2, autenticação acessível).
- **`autoFocus` com parcimônia** — só desktop, num único input primário; evitar em mobile (abre o teclado e desloca a tela).
- **Avisar antes de sair** com alterações não salvas (guard de rota do React Router / `beforeunload`).

### 12.2 Estado refletido na URL (complementa §8)

Filtros, abas, paginação e painéis expandidos devem viver em **query params** via **`useSearchParams` do React Router** (não `nuqs`/Next). Assim a tela é *deep-linkável*, sobrevive a refresh e funciona com voltar/avançar. Links de navegação usam `<Link>`/`<a>` (suporte a Ctrl/Cmd+click e botão do meio) — **nunca** `onClick` em `div`/`span`.

### 12.3 Tipografia (complementa §9)

- **`tabular-nums`** em colunas de números, tabelas, `StatCard` e tooltips/eixos de gráfico (recharts) — alinha dígitos e evita “pulos”.
- **`text-balance`** em títulos (evita órfãos/viúvas); `text-pretty` em parágrafos longos.
- Reticências de verdade `…` (não `...`); aspas curvas `“ ”`; loading termina em `…` (`"Carregando…"`); espaço não-quebrável em unidades/atalhos (`10 MB`, `⌘ K`).

### 12.4 Mobile & touch (complementa §8)

- **`touch-action: manipulation`** em elementos clicáveis (elimina o atraso do double-tap zoom).
- **Alvos ≥ 24px** (44px ideal em mobile) — ver §1.2.
- **`overscroll-behavior: contain`** em modais, sheets e drawers (evita “vazar” o scroll para a página).
- Durante *drag*: desabilitar seleção de texto e marcar elementos arrastados como `inert`. Sempre oferecer alternativa sem arrastar (§1.2).
- `-webkit-tap-highlight-color` definido intencionalmente; layouts full-bleed respeitam `env(safe-area-inset-*)` (o `viewport-fit=cover` já está no `index.html`). **Não** desabilitar zoom (`user-scalable=no`/`maximum-scale=1`).

### 12.5 Imagens (complementa §6)

- `width`/`height` **explícitos** para evitar *layout shift* (CLS); `alt` significativo (ou `alt=""` se decorativa).
- `loading="lazy"` abaixo da dobra; `fetchpriority="high"` em imagem crítica acima da dobra.
- Texto longo de usuário tratado com `truncate`/`line-clamp-*`; filhos de flex precisam de `min-w-0` para truncar.

### 12.6 Dark mode (complementa §3)

- Declarar **`color-scheme`** no `:root`/`.dark` de [`src/index.css`](../src/index.css) (corrige scrollbars e controles nativos no tema escuro).
- **`<meta name="theme-color">`** no [`index.html`](../index.html) casando com o fundo da página (idealmente um por tema).
- `<select>` nativo: definir `background-color` e `color` explícitos (dark mode do Windows).

### 12.7 Locale & i18n (produto pt-BR / R$)

- **Datas/horas:** `Intl.DateTimeFormat` (não formato cravado na mão).
- **Números/moeda:** `Intl.NumberFormat` com `pt-BR`/`BRL` — nunca concatenar `"R$ " + valor`.
- **Idioma:** detectar via `navigator.languages`, não por IP.
- **Nomes de marca e tokens de código** (“EvoSales”, “ProspectAI”, IDs, comandos): `translate="no"` para o tradutor automático do navegador não corromper.

### 12.8 Performance (complementa §8)

- **Listas grandes (> 50 itens)** — ex.: tabela de leads, histórico de atividades — **virtualizar** (`virtua`, `@tanstack/react-virtual` ou `content-visibility: auto`). Evitar `.map()` em arrays enormes sem virtualização.
- **Sem leitura de layout no render** (`getBoundingClientRect`, `offsetHeight`, `scrollTop`); agrupe leituras/escritas de DOM.
- Preferir **inputs não-controlados** quando possível; input controlado deve ser barato por tecla.
- `<link rel="preconnect">` para domínios de CDN/assets; fontes críticas com `<link rel="preload" as="font">` + `font-display: swap`.

---

## 13. Checklist para nova tela / refatoração

- [ ] Pesquisei o padrão em UIs de referência e adaptei aos tokens daqui.
- [ ] Desenhei em **tons de cinza primeiro**; cor entrou só para reforçar a hierarquia.
- [ ] **Hierarquia**: uma ação primária óbvia; tamanho/peso/cor refletem importância.
- [ ] **Proximidade**: itens relacionados agrupados; ação destrutiva isolada.
- [ ] **Divulgação progressiva**: fluxo longo dividido em etapas com indicador.
- [ ] **Acessibilidade (WCAG 2.2 AA)**: `focus-visible`, `aria-label` em botão só-ícone, `alt`, teclado, **alvos ≥ 24px**, foco não obscurecido pelo header, alternativa a arrastar, colar permitido em senha/OTP, **skip link** e `aria-live` em updates assíncronos.
- [ ] **Contraste validado (§3.4)**: texto/ícone sobre cor ≥ 4.5:1 (texto) / 3:1 (grande e UI). Sem `text-white` cru sobre o teal do light.
- [ ] Usei tokens de cor (sem hex/`style` inline); funciona em **light e dark**.
- [ ] Reutilizei `Button`, `Card`, `Badge`, `HeaderSection`, status pills existentes.
- [ ] Botões: ação primária à direita, `DialogFooter` em modais; estados interativos com mais contraste.
- [ ] **Sem sombras** em conteúdo (só overlays).
- [ ] Estados de **loading, vazio e erro** tratados (erro explica o próximo passo).
- [ ] Animações sutis: só `transform`/`opacity`, sem `transition: all`, interrompíveis.
- [ ] `border-radius` `rounded-md` (cards `rounded-lg`), espaçamento `space-y-6`/`gap-*` (escala restrita).
- [ ] **Formulários** (§12.1): erro inline + foco no 1º erro; submit com spinner; `type`/`autocomplete` corretos; input mobile ≥ 16px.
- [ ] **Estado na URL** (§12.2): filtros/abas/paginação em `useSearchParams`; navegação via `<Link>`.
- [ ] **Números** (§12.3): `tabular-nums`. **Datas/moeda** (§12.7): `Intl.*` com pt-BR/BRL.
- [ ] **Performance** (§12.8): listas > 50 itens virtualizadas.

---

## 14. Manutenção

Atualize este documento sempre que: novas cores/tokens entrarem, um componente base mudar, ou um padrão novo for acordado. Mantenha alinhado com [`src/index.css`](../src/index.css) e [`src/components/ui/`](../src/components/ui/), e **mantenha os dois conjuntos de token (§3.1 e §3.2) em sincronia**.

Fundamentos de UI/UX baseados nos [7 princípios de UI Design da Figma](https://www.figma.com/pt-br/resource-library/principios-design-ui/) (ver §1.1). Para arquitetura/estrutura de código, ver [`docs/estrutura-sistema.md`](./estrutura-sistema.md).

---

## 15. Referências & histórico de revisão

### Fontes consultadas (jun/2026)

- **WCAG 2.2** (W3C Recommendation, atual; ISO/IEC 40500:2025) — critérios AA novos: alvo mínimo 24px (2.5.8), foco não obscurecido (2.4.11), alternativas a arrastar (2.5.7), autenticação acessível (3.3.8), ajuda consistente (3.2.6), entrada redundante (3.3.7).
- **Vercel Web Interface Guidelines** — `github.com/vercel-labs/web-interface-guidelines` (regras de acessibilidade, foco, formulários, animação, performance, touch, dark mode, i18n e copy).
- **Refactoring UI** (Adam Wathan & Steve Schoger) — hierarquia por tamanho/peso/cor, “tons de cinza primeiro”, cor semântica ≠ peso visual, escalas restritas de espaçamento, *whitespace*.
- **Design tokens** — modelo de três camadas (primitivo → semântico → componente) e nomeação por intenção (ex.: Carbon/IBM; Smashing Magazine; comunidade Figma).

### O que mudou nesta revisão

1. **Acessibilidade migrou para WCAG 2.2 AA** (§1.2), com critérios novos diretamente relevantes (alvo de toque, foco sob header sticky, alternativa a arrastar no funil, colar em senha/OTP, ajuda consistente). Adicionados **skip link** e **`aria-live`**.
2. **Correção de contraste do `primary`** (§3.4): o teal do light com texto branco reprova em AA (2.09:1). Introduzidos os tokens `on-primary` e `primary-strong`, com duas estratégias de correção e uma regra permanente de verificação. O exemplo da §3.3 foi atualizado de `text-white` para `text-on-primary`.
3. **Esclarecida a relação entre os dois conjuntos de token** (§3.0): `--color-*` (app) e HSL shadcn (componentes) são a mesma camada semântica, mantida em sincronia.
4. **Princípio “tons de cinza primeiro”** adicionado (§1, pilar de hierarquia) e ênfase em “cor semântica ≠ peso visual” (§1.1, §4).
5. **Regras técnicas de animação** (§11): só `transform`/`opacity`, nunca `transition: all`, `transform-origin`, SVG `transform-box`, interrompíveis.
6. **Novas subseções**: i18n com `Intl.*` e `translate="no"` (§12.7) e **Performance** com virtualização de listas (§12.8) — relevante para tabelas de leads.
7. **Inputs** (§6.4/§12.1): `font-size` ≥ 16px no mobile, `aria-describedby`/`aria-invalid`, `autocomplete="off"` em campos não-auth, `autoFocus` só desktop.
8. **Toasts** (§5): `aria-live` por variante, pausa no hover, copy com próximo passo, reforço de que toast não substitui erro inline.
9. **Estados de erro/vazio** (§8) reescritos com foco em direção (“o que fazer”), na voz do produto.
10. Tipografia do próprio documento ajustada às regras que ele prega (`…`, aspas curvas) e **checklist atualizado** (§13).
11. **`secondary` migrou de roxo para azul** (jun/2026): o roxo `#5549df` foi aposentado e o token passou a `#2563eb` (blue-600, HSL `221 83% 53%`) nos dois temas — §3.1/§3.2/§3.4/§4.3. Adicionados também os tokens `--color-status-process`/`--color-status-lost` e `--color-success-strong`/`--color-destructive-strong` (§3.1). Pendente: cores **hardcoded** da `LeadStatusPill` (§4.1) ainda não foram migradas para tokens — ex.: violet do "Novo", slate do "Parado".

12. **Status `STOPPED` ("Parado", slate)** adicionado à `LeadStatusPill` (§4.1) — sétimo status de lead, alinhado com a fonte única `LEAD_STATUS_META`.