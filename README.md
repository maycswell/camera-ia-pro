# CÂMERA IA PRO — versão inicial

Projeto Android nativo em Kotlin com CameraX.

## Recursos desta primeira versão
- Visualização ao vivo da câmera.
- Captura em qualidade priorizada pelo CameraX.
- Salvamento em `Pictures/CameraIAPro`.
- Alternância entre câmera frontal e traseira.
- Controle básico de flash.
- Seleção de modo Terra ou Espaço (rótulos e orientação de uso).

## Limitações honestas
Esta versão ainda não contém processamento de imagem por IA, HDR avançado,
modo astrofotográfico real, zoom avançado ou edição. O modo Espaço não transforma
a câmera do celular em um telescópio: o resultado depende do sensor, da lente,
da exposição e das condições do céu.

## Como gerar o APK sem computador
1. Extraia o ZIP no celular.
2. Crie uma conta gratuita no GitHub, se ainda não tiver.
3. No navegador do Android, abra GitHub e crie um repositório público chamado `camera-ia-pro`.
4. Envie o conteúdo desta pasta para a raiz do repositório (não envie a pasta externa inteira).
5. Abra a aba **Actions** do repositório.
6. Selecione **Build Camera IA Pro APK** e toque em **Run workflow**. Também pode compilar automaticamente após enviar os arquivos para a branch `main`.
7. Quando terminar, abra a execução concluída e baixe o artefato `CameraIAPro-debug`.
8. Extraia o arquivo baixado e instale `app-debug.apk`. O Android pode pedir autorização para instalar pelo navegador/gerenciador de arquivos.

## Requisitos de compilação
- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK 35

O APK gerado é de depuração (debug), adequado para testes. Para distribuição pública,
será necessário configurar assinatura de lançamento (release).
