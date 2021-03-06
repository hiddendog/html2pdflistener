# html2pdflistener

Integração do FlyingSaucer com o JSF


## AVISO IMPORTANTE

A partir da versão 2.0.0-BETA1, esta biblioteca passou a utilizar o openhtml2pdf,
que trocou uma versão antiga do iText (pois as novas não permitem uso comercial)
pelo PDFBox. 

## Objetivo

O objetivo desta biblioteca é permitir que qualquer página HTML de um projeto
JSF possa ser convertido em PDF para ser mostrado diretamente ao usuário ou
então disponibilizado para outra ação JSF.

## Instalando

Adicione o repositório no seu pom.xml

    <repository>
        <id>html2pdflistener.repo</id>
        <name>html2pdflistener.repo</name>
        <url>https://raw.github.com/joaomc/html2pdflistener-repo/master/repository/</url>
    </repository>

Depois, adicione a dependência no seu pom.xml

    <dependency>
        <groupId>br.com.christ.jsf</groupId>
        <artifactId>html2pdflistener</artifactId>
        <version>2.0.0-BETA2</version>
    </dependency>

## Renderizando um template .xhtml para PDF

Você pode obter renderizar uma view .html para PDF de qualquer parte do sistema, utilizando o método abaixo:

    byte[] output = FaceletsConverter.renderFaceletAsPDF(config, paginaDestino);

Dica: utilize beans RequestScoped ou ViewScoped para configurar dados que devem ser exibidos no PDF.

O primeiro parâmetro é uma instância de PDFConverterConfig. O segundo é a página de destino. Exemplo:

    PDFConverterConfig config = new DefaultPDFConverterConfig();
    config.setFileName("meuarquivo.pdf");
    config.setPreloadResources(true);
    byte[] output = FaceletsConverter.renderFaceletAsPDF(config, "minhapagina.xhtml");

## Problemas ao carregar imagens, CSS, etc

Caso o PDF gerado esteja sem a formatação adequada ou sem imagens, provavelmente está
havendo problemas ao carregar as imagens ou CSS. Uma forma de resolver o problema é
fazer o embed de imagens em Base64 e inserir o CSS diretamente na página ao invés de
fazer referências. Você pode também utilizar a funcionalidade de preload de imagens para
evitar que o gerador precise fazer o download durante a geração do PDF. Para isso, insira
antes do "src" da imagem a tag "preload:". Exemplo:

    <img src="preload:/public/resources/images/logo.png" />

Dessa maneira, a imagem será carregada no listener JSF. Isso é necessário caso haja:

 * Problemas ao carregar arquivos de servidor HTTPS
 * Recursos que estiverem em um local com acesso restrito (login necessário)

O componente irá precarregar automaticamente as imagens e stylesheets se for detectada uma
conexão HTTPS. Além disso, você pode forçar o pré-carregamento de imagens e CSS passando o
atributo de request "preload_resources":

    public String gerarRelatorioEmail() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        pdfConverterConfig.setEnablePdf(true);
        pdfConverterConfig.setFileName("relatorio.pdf");
        pdfConverterConfig.setPreloadResources(true);
        return "relatorio";
    }

## Problemas de codificação

A partir da versão 1.1.17, a codificação é passada para o JSoup, evitando problemas de acentuação.

Caso mesmo assim você tenha problemas de codificação, pode informar manualmente a codificação do
PDF gerado:

    public String gerarRelatorioEmail() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        pdfConverterConfig.setEnablePdf(true);
        pdfConverterConfig.setEncoding("iso-8859-1");
        return "relatorio";
    }

## FAQ


### A página é mostrada diretamente na tela, não está sendo gerado o PDF!

* Tome cuidado com as seguintes propriedades CSS, elas podem causar problemas!
  * float
  * position: fixed
* Verifique possíveis exceções no log da aplicação.

### O PDF demora demais para ser gerado!

Isso costuma acontecer quando há propriedades float que impedem que o cálculo
das dimensões dos objetos seja feito corretamente para a geração do PDF. Retire
as propriedades float, de preferência utilizando um CSS específico para
impressão.

### Como eu incluo cabeçalhos e rodapés na página?

O FlyingSaucer suporta atributos CSS3 para inclusão de cabeçalhos, rodapés e
afins. Confira em [http://www.w3.org/TR/css3-gcpm/](http://www.w3.org/TR/css3-gcpm/).
Segue um exemplo:

    <style type="text/css" media="all">
        .footer {
            position: running(footer);
        }

        .header {
            position: running(header);
        }

        @page {
            @bottom-right {
                content: element(footer);
            }
            @top-left {
                content: element(header);
            }
        }

        #pagenumber:before {
            content: counter(page);
        }

        #pagecount:before {
            content: counter(pages);
        }
    </style>
    <div class="footer">
        Rodapé, página <span id="pagenumber"></span> of <span id="pagecount"></span>
    </div>
    <div class="header">
        Cabeçalho da pagina
    </div>

### Como eu inclui algo como "Página 2 de 10" no PDF?

Veja o exemplo acima!

### Eu fiz isso, mas os cabeçalhos e rodapés não aparecem em todas as páginas!

Os elementos utilizados como cabeçalho e/ou rodapé devem estar logo no começo da página,
logo depois da tag "body". Por exemplo, isso não funciona:

    <body>
        <div class="header">
            Meu cabeçalho
        </div>
        <!-- Conteúdo da página -->
        teste
        <div class="footer">
            Meu rodapé
        </div>
    </body>
    
Mas isso funciona:

    <body>
        <div class="header">
            Meu cabeçalho
        </div>
        <div class="footer">
            Meu rodapé
        </div>
        <!-- Conteúdo da página -->
        teste
    </body>
    
    
Não se preocupe, o posicionamento dos cabeçalhos e rodapés é definido por CSS, 
então não há problemas em se colocar o elemento rodapé no começo da página!



### Como eu executo algum código antes ou depois de gerado o PDF?

A classe PDFConverterConfig permite que você execute trechos de código antes ou depois
da conversão, através do uso de listeners:

    pdfConfig.setListeners(new ConversionListener() {
        @Override
        public boolean beforeConvert(ConverterContext context) throws IOException {
            System.out.println(facesContext.getViewRoot().getViewId());
            return true;
        }

        @Override
        public boolean afterConvert(ConverterContext context) {
            return true;
        }

        @Override
        public boolean afterResponseComplete(ConverterContext context) {
            return true;
        }
    });

Há uma implementação de listener específica para gerar/remover cookies:

    pdfConfig.setListeners(CookieOperations.doDelete("relnovo"),
                           CookieOperations.doAdd("crieiRelatorio","1"));


### Eu posso usar CSS específico para a geração do PDF?

Sim, o FlyingSaucer utiliza os estilos com media="print", ou seja, você normalmente
pode criar um estilo específico para impressão, e este será também usado para a geração de PDF.
Caso isso não seja satisfatório, pode configurar a remoção automática de todos os
 estilos CSS de tags que não contenham o atributo "data-pdf-preserve" igual a "true"
 e que não tenham "pdf" no atributo "media"


    pdfConfig.setRemoveStyles(true);

Dessa maneira, os estilos abaixo não serão carregados:

    <link rel="stylesheet" href="meucss.css" />
    <style type="text/css">
        // alguns estilos
    </style>
    <style type="text/css" media="print">
        // alguns estilos
    </style>

Os estilos abaixo seriam carregados na geração do PDF:

    <style type="text/css" data-pdf-preserve="true">
        // alguns estilos
    </style>
    <link rel="stylesheet" href="meucss.css" media="pdf" />
