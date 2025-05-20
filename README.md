# MagicCube3D

**MagicCube3D** é uma aplicação JavaFX para simulação interativa de um Cubo Mágico 3D. Este projeto é desenvolvido para a disciplina de Programação Orientada à Objetos (CCMP0151).

## 🎯 Objetivos

* Oferecer uma ferramenta educacional e lúdica para:
  * Visualizar algoritmos de resolução do cubo.
  * Simular movimentos e estudar padrões.
  * Auxiliar no aprendizado de iniciantes com tutoriais interativos.
  * Servir como substituto digital de cubos físicos.

## ✨ Funcionalidades Essenciais

* Visualização 3D interativa do Cubo Mágico.
* Manipulação básica das faces do cubo.
* Interface gráfica intuitiva.

## 🛠️ Tecnologias

* **Java 24**
* **JavaFX (com JavaFX 3D)**
* **Scene Builder**
* **Git/GitHub**

## 📁 Estrutura do Projeto

```
projeto-ccmp0151/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/univasf/magiccube3d/  // Pacote raiz da aplicação
│   │   │       ├── MainApp.java          // Classe principal que inicia a aplicação
│   │   │       ├── model/                // Classes de modelo de dados
│   │   │       │   ├── Cube.java         // Lógica e estado do Cubo Mágico
│   │   │       │   └── Facelet.java      // Representa uma peça colorida do cubo
│   │   │       ├── controller/           // Classes de controle (lógica da UI)
│   │   │       │   └── RubikController.java // Gerencia interações do usuário
│   │   │       └── view/                 // Classes relacionadas à visualização
│   │   │           └── CubeView.java     // Responsável pela renderização 3D do cubo
│   │   └── resources/                    // Recursos não-código
│   │       └── com/univasf/magiccube3d/
│   │           ├── view/
│   │           │   └── RubikView.fxml    // Arquivo FXML para a UI principal
│   │           └── styles/
│   │               └── style.css         // Folha de estilos CSS
├── .gitignore                            // Arquivos e pastas a serem ignorados pelo Git
└── README.md                             // Este arquivo
```

## 🚀 Execução

### Como executar

1. Clone o repositório:

  ```bash
  git clone https://github.com/seu-usuario/projeto-ccmp0151.git
  cd projeto-ccmp0151
  ```

2. Compile e execute a aplicação usando o Maven Wrapper:

  ```bash
  ./mvnw javafx:run
  ```

  > No Windows, use `mvnw.cmd javafx:run`.

3. Você também pode executar a classe principal `com.univasf.magiccube3d.MainApp` diretamente pela sua IDE.

> Este é um projeto acadêmico desenvolvido para fins educacionais.
