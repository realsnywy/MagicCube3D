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
  git clone https://github.com/realsnywy/projeto-ccmp0151.git
  cd projeto-ccmp0151
  ```

2. Compile o projeto usando o Maven Wrapper:

  ```bash
  ./mvnw clean install    # Para Linux/macOS
  mvnw.cmd clean install  # Para Windows
  ```

3. Execute a aplicação:

  ```bash
  ./mvnw javafx:run       # Para Linux/macOS
  mvnw.cmd javafx:run     # Para Windows
  ```

4. Você também pode executar a classe principal `com.univasf.magiccube3d.MainApp` diretamente pela sua IDE.

---

## ⌨️ Atalhos de Teclado

Você pode controlar o cubo usando os seguintes atalhos de teclado:

### Rotação das faces e centros

| Tecla | Movimento         | Tecla (inversa) | Movimento inverso   |
|-------|-------------------|-----------------|---------------------|
| U     | Up (U)            | Q               | Up' (U')            |
| E     | Eixo X (E)        | W               | Eixo X' (E')        |
| F     | Front (F)         | G               | Front' (F')         |
| S     | Eixo S (S)        | A               | Eixo S' (S')        |
| B     | Back (B)          | V               | Back' (B')          |
| D     | Down (D)          | X               | Down' (D')          |
| L     | Left (L)          | K               | Left' (L')          |
| M     | Eixo M (M)        | N               | Eixo M' (M')        |
| R     | Right (R)         | T               | Right' (R')         |

### Outros comandos

| Tecla         | Função                |
|---------------|----------------------|
| Espaço (Space)| Embaralhar (Shuffle) |
| Backspace     | Resetar cubo         |
| Numpad 8/2/4/6| Girar visualização   |
| Numpad 7/9    | Girar eixo Z         |

**Observação:** Os atalhos funcionam apenas quando o cubo está com o foco (clique sobre o cubo antes de usar o teclado).

---

> Este projeto é de caráter acadêmico e foi desenvolvido exclusivamente para fins educacionais.
