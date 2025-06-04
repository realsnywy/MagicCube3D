package com.univasf.magiccube3d.util;

/**
 * Reprodutor de módulos Protracker (MOD).
 * Este código é uma adaptação do ModPlay3 original encontrado no repositório
 * martincameron/tracker3.
 * Modificado por Gabriel "Snywy" Furtado, 2025.
 */
public class ModPlay3 {
	private static final int MAX_SAMPLES = 32; // Número máximo de amostras (instrumentos).
	private static final int MAX_CHANNELS = 8; // Número máximo de canais de áudio.
	private static final int FIXED_POINT_SHIFT = 13; // Deslocamento para aritmética de ponto fixo.
	private static final int FIXED_POINT_ONE = 1 << FIXED_POINT_SHIFT; // Valor 'um' em ponto fixo.

	// Coeficientes do filtro (provavelmente para downsampling ou efeito).
	private static final int[] FILTER_COEFFS = {
			-512, 0, 4096, 8192, 4096, 0, -512
	};

	// Tabela de mapeamento de notas (teclas) para períodos Protracker.
	// Períodos são inversamente proporcionais à frequência.
	private static final short[] KEY_TO_PERIOD = {
			1814, /*
						 * Mapeamento de notas para períodos.
						 * C-0 C#0 D-0 D#0 E-0 F-0 F#0 G-0 G#0 A-1 A#1 B-1 (Notação de Oitava/Nota)
						 */
			1712, 1616, 1524, 1440, 1356, 1280, 1208, 1140, 1076, 1016, 960, 907, // C-1 a B-1
			856, 808, 762, 720, 678, 640, 604, 570, 538, 508, 480, 453, // C-2 a B-2 (C-2 é a base para c2Rate)
			428, 404, 381, 360, 339, 320, 302, 285, 269, 254, 240, 226, // C-3 a B-3
			214, 202, 190, 180, 170, 160, 151, 143, 135, 127, 120, 113, // C-4 a B-4
			107, 101, 95, 90, 85, 80, 75, 71, 67, 63, 60, 56, // C-5 a B-5
			53, 50, 47, 45, 42, 40, 37, 35, 33, 31, 30, 28, 26 // C-6 a D#6 (limite superior)
	};

	// Tabela de ajuste fino para períodos (multiplicadores em ponto fixo).
	private static final short[] FINE_TUNE = {
			8192, 8133, 8075, 8016, 7959, 7902, 7845, 7788, // Finetune +0 a +7
			8679, 8617, 8555, 8493, 8432, 8371, 8311, 8251 // Finetune -8 a -1 (armazenado como 8-15)
	};

	// Tabela de forma de onda para o efeito de vibrato (meio ciclo de uma senoide).
	private static final short[] VIBRATO = {
			0, 24, 49, 74, 97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
			255, 253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120, 97, 74, 49, 24
	};

	private String songName; // Nome da música.
	private String[] instrumentNames = new String[MAX_SAMPLES]; // Nomes dos instrumentos/amostras.
	private int numChannels; // Número de canais de áudio usados pela música.
	private int patChannels; // Número de canais por padrão (pattern) no arquivo.
	private int songLength; // Comprimento da música na sequência de padrões.
	private int c2Rate; // Taxa de amostragem de referência para a nota C-2 (Dó na segunda oitava),
											// varia com o número de canais.
	private byte[] sequence; // Sequência de padrões da música (ordem de reprodução dos padrões).
	private byte[] patternData; // Dados dos padrões (notas, efeitos, etc.).
	private byte[][] sampleData = new byte[MAX_SAMPLES][]; // Dados brutos das amostras de áudio (instrumentos).
	private int[] sampleFineTune = new int[MAX_SAMPLES]; // Ajuste fino para cada amostra (0-15, onde 8-15 são negativos).
	private int[] sampleVolume = new int[MAX_SAMPLES]; // Volume padrão para cada amostra (0-64).
	private int[] sampleLoopStart = new int[MAX_SAMPLES]; // Ponto de início do loop para cada amostra (em ponto fixo).
	private int[] sampleLoopLength = new int[MAX_SAMPLES]; // Comprimento do loop para cada amostra (em ponto fixo).

	// Estado de cada canal durante a reprodução
	private int[] channelInstrument = new int[MAX_CHANNELS]; // Instrumento atualmente tocando em cada canal.
	private int[] channelAssigned = new int[MAX_CHANNELS]; // Instrumento atribuído à nota no padrão para cada canal
																													// (usado para efeitos como note delay).
	private int[] channelEffect = new int[MAX_CHANNELS]; // Efeito atual em cada canal.
	private int[] channelParameter = new int[MAX_CHANNELS]; // Parâmetro do efeito atual em cada canal.
	private int[] channelVolume = new int[MAX_CHANNELS]; // Volume atual de cada canal (0-64).
	private int[] channelPanning = new int[MAX_CHANNELS]; // Posicionamento estéreo (panning) de cada canal (0 a
																												// FIXED_POINT_ONE).
	private int[] channelPeriod = new int[MAX_CHANNELS]; // Período (inverso da frequência) da nota atual em cada canal.
	private int[] channelSamplePos = new int[MAX_CHANNELS]; // Posição atual na amostra para cada canal (em ponto fixo).
	private int[] channelFrequency = new int[MAX_CHANNELS]; // Frequência de reprodução da amostra em cada canal
																													// (calculada a partir do período).
	private int[] channelArpeggio = new int[MAX_CHANNELS]; // Deslocamento de semitons para o efeito de arpejo em cada
																													// canal.
	private int[] channelVibrato = new int[MAX_CHANNELS]; // Deslocamento de período para o efeito de vibrato em cada
																												// canal.
	private int[] channelVibratoSpeed = new int[MAX_CHANNELS]; // Velocidade do vibrato em cada canal.
	private int[] channelVibratoDepth = new int[MAX_CHANNELS]; // Profundidade do vibrato em cada canal.
	private int[] channelVibratoPhase = new int[MAX_CHANNELS]; // Fase atual do vibrato em cada canal.
	private int[] channelPortaPeriod = new int[MAX_CHANNELS]; // Período alvo para o efeito de portamento em cada canal.
	private int[] channelPortaSpeed = new int[MAX_CHANNELS]; // Velocidade do portamento em cada canal.
	private int[] channelTremolo = new int[MAX_CHANNELS]; // Deslocamento de volume para o efeito de tremolo em cada
																												// canal.
	private int[] channelTremoloSpeed = new int[MAX_CHANNELS]; // Velocidade do tremolo em cada canal.
	private int[] channelTremoloDepth = new int[MAX_CHANNELS]; // Profundidade do tremolo em cada canal.
	private int[] channelPatternLoopRow = new int[MAX_CHANNELS]; // Linha de início do loop de padrão (efeito E6x) para
																																// cada canal.
	private int[] channelSampleOffset = new int[MAX_CHANNELS]; // Deslocamento inicial da amostra (efeito 9xx) em cada
																															// canal (em ponto fixo).

	// Estado global do sequenciador
	private int currentSequencePos; // Posição atual na sequência de padrões.
	private int nextSequencePos; // Próxima posição na sequência de padrões (após pattern break/jump).
	private int currentRow; // Linha atual no padrão (0-63).
	private int nextRow; // Próxima linha no padrão.
	private int currentTick; // Tick atual dentro da linha (usado para timing de efeitos e velocidade).
	private int ticksPerRow; // Número de ticks por linha (controla a velocidade, padrão é 6).
	private int tempo; // Tempo da música (BPM - batidas por minuto, padrão é 125).
	private int effectCounter; // Contador para efeitos que ocorrem em ticks específicos (ex: arpeggio).
	private int patternLoopCount; // Contador para o efeito de loop de padrão (E6x).
	private int patternLoopChannel; // Canal que iniciou o loop de padrão.
	private int mute; // Máscara de bits para silenciar canais (1 << channelNum).
	private boolean sequencerEnabled = true; // Flag para habilitar/desabilitar o processamento do sequenciador.
	private int[] rampBuf = new int[64]; // Buffer para suavização de volume (volume ramping) entre blocos de áudio.

	/**
	 * Construtor para criar um módulo vazio com um número específico de canais.
	 *
	 * @param numChannels O número de canais para o novo módulo.
	 */
	public ModPlay3(int numChannels) {
		songName = ""; // Inicializa o nome da música.
		for (int idx = 1; idx < MAX_SAMPLES; idx++) {
			// Inicializa nomes de instrumentos, volumes e dados de amostra.
			instrumentNames[idx] = "";
			sampleVolume[idx] = 64; // Volume máximo padrão.
			sampleData[idx] = new byte[0]; // Amostra vazia.
		}
		songLength = 1; // Comprimento mínimo da música.
		sequence = new byte[1]; // Sequência mínima com um padrão.
		setNumChannels(numChannels); // Define o número de canais.
		// Configura os dados do padrão (vazios) e a posição inicial da sequência.
		setPatternData(new byte[numChannels * 4 * 64], numChannels);
		setSequencePos(0, 0); // Inicia no primeiro padrão, primeira linha.
	}

	/**
	 * Construtor para carregar um módulo a partir de um InputStream.
	 * Se 'soundtracker' for verdadeiro, os dados do módulo são assumidos como
	 * estando no formato original Ultimate Soundtracker.
	 *
	 * @param moduleData   O InputStream contendo os dados do módulo.
	 * @param soundtracker true se o formato for Ultimate Soundtracker, false para
	 *                     Protracker e compatíveis.
	 * @throws java.io.IOException      Se ocorrer um erro de I/O durante a leitura.
	 * @throws IllegalArgumentException Se o formato do módulo não for reconhecido
	 *                                  ou for inválido.
	 */
	public ModPlay3(java.io.InputStream moduleData, boolean soundtracker) throws java.io.IOException {
		songName = readString(moduleData, 20); // Lê o nome da música (20 bytes).
		int numSamples = soundtracker ? 16 : 32; // Soundtracker usa 15 amostras (índice 1-15), Protracker usa 31 (índice
																							// 1-31).
		int[] sampleLengths = new int[numSamples]; // Armazena os comprimentos das amostras lidos do arquivo.

		// Lê informações de cada amostra
		for (int idx = 1; idx < numSamples; idx++) {
			instrumentNames[idx] = readString(moduleData, 22); // Nome da amostra (22 bytes).
			sampleLengths[idx] = (((moduleData.read() & 0xFF) << 8) | (moduleData.read() & 0xFF)) * 2; // Comprimento em bytes
																																																	// (armazenado como
																																																	// palavras).
			sampleFineTune[idx] = moduleData.read() & (soundtracker ? 0 : 0xF); // Ajuste fino (0-15), ignorado por
																																					// Soundtracker.
			sampleVolume[idx] = moduleData.read() & 0x7F; // Volume (0-64).
			int loopStart = (((moduleData.read() & 0xFF) << 8) | (moduleData.read() & 0xFF)) * (soundtracker ? 1 : 2); // Início
																																																									// do
																																																									// loop
																																																									// em
																																																									// bytes.
			int loopLength = (((moduleData.read() & 0xFF) << 8) | (moduleData.read() & 0xFF)) * 2; // Comprimento do loop em
																																															// bytes.

			// Validação e ajuste do loop
			if (loopLength < 4 || loopStart > sampleLengths[idx]) { // Loop inválido ou início após o fim da amostra.
				loopStart = sampleLengths[idx]; // Define o loop no fim da amostra (sem loop efetivo).
			}
			if (loopStart + loopLength > sampleLengths[idx]) { // Loop excede o tamanho da amostra.
				loopLength = sampleLengths[idx] - loopStart; // Ajusta o comprimento do loop.
			}
			sampleLoopStart[idx] = loopStart * FIXED_POINT_ONE; // Armazena em ponto fixo.
			sampleLoopLength[idx] = loopLength * FIXED_POINT_ONE; // Armazena em ponto fixo.
		}

		// Inicializa amostras não usadas (para módulos com menos de MAX_SAMPLES).
		for (int idx = numSamples; idx < MAX_SAMPLES; idx++) {
			instrumentNames[idx] = "";
			sampleVolume[idx] = 64;
			sampleData[idx] = new byte[0];
		}

		songLength = moduleData.read() & 0x7F; // Comprimento da sequência de padrões (1-127).
		if (songLength < 1) {
			songLength = 1; // Garante comprimento mínimo.
		}
		// Posição 127 do cabeçalho (byte de "restart position") é ignorada aqui, mas
		// lida como parte da sequência.
		moduleData.read(); // Lê e descarta o byte de "restart position"
		sequence = readBytes(moduleData, 128); // Lê a tabela de sequência de padrões (128 bytes).

		// Determina o número total de padrões únicos no módulo.
		int numPatterns = 0;
		for (int idx = 0; idx < 128; idx++) {
			if (numPatterns < sequence[idx] + 1) {
				numPatterns = sequence[idx] + 1;
			}
		}

		// Identificador do tipo de módulo (M.K., FLT4, etc.) para determinar o número
		// de canais.
		String modType = soundtracker ? "M.K." : readString(moduleData, 4);
		if (modType.equals("M.K.") || modType.equals("M!K!") || modType.equals("FLT4")) {
			setNumChannels(4);
		} else if (modType.equals("CD81") || modType.equals("OKTA")) { // Formatos de 8 canais
			setNumChannels(8);
		} else if (modType.length() > 0 && modType.substring(1).equals("CHN")) { // Formatos NCHN (ex: 6CHN)
			setNumChannels(modType.charAt(0) - '0');
		}

		if (numChannels < 1 || numChannels > MAX_CHANNELS) {
			throw new IllegalArgumentException("Módulo não reconhecido ou número de canais inválido!");
		}

		// Lê e configura os dados dos padrões.
		setPatternData(readBytes(moduleData, numChannels * 4 * 64 * numPatterns), numChannels);

		// Converte os dados do padrão para um formato interno mais conveniente.
		// O formato original armazena período e parte do instrumento no primeiro word,
		// e o restante do instrumento e efeito/parâmetro no segundo word.
		for (int idx = 0; idx < patternData.length; idx += 4) {
			// Extrai informações do formato Protracker.
			int key = periodToKey(((patternData[idx] & 0xF) << 8) | (patternData[idx + 1] & 0xFF)); // Converte período para
																																															// nota.
			int instrument = (soundtracker ? 0 : patternData[idx] & 0x10) | ((patternData[idx + 2] >> 4) & 0xF); // Combina
																																																						// partes do
																																																						// número do
																																																						// instrumento.
			int effect = patternData[idx + 2] & 0xF; // Código do efeito.
			int param1 = (patternData[idx + 3] >> 4) & 0xF; // Nibble superior do parâmetro.
			int param2 = patternData[idx + 3] & 0xF; // Nibble inferior do parâmetro.

			// Ajustes específicos para o formato Soundtracker (efeitos 1 e 2 são
			// diferentes).
			if (soundtracker) {
				if (effect == 1) { // Arpeggio em ST (0xy)
					effect = 0; // Mapeia para Arpeggio (0xy) em PT
				} else if (effect == 2) { // Pitch slide em ST (2xy)
					if ((param1 - param2) < 0) { // param1 < param2 -> Slide Down by (param2 - param1)
						effect = 2; // Mapeia para Portamento Down (2xx) em PT
						param2 = param2 - param1; // Calcula a velocidade
						param1 = 0;
					} else { // param1 >= param2 -> Slide Up by (param1 - param2)
						effect = 1; // Mapeia para Portamento Up (1xx) em PT
						param2 = param1 - param2; // Calcula a velocidade
						param1 = 0;
					}
				} else { // Outros efeitos não mapeados diretamente são zerados.
					effect = param1 = param2 = 0;
				}
			}
			// Armazena no formato interno: [key, instrument, effect, (param1<<4)|param2]
			patternData[idx] = (byte) key;
			patternData[idx + 1] = (byte) instrument;
			patternData[idx + 2] = (byte) effect;
			patternData[idx + 3] = (byte) ((param1 << 4) | (param2 & 0xF));
		}

		// Lê os dados de cada amostra.
		for (int idx = 1; idx < numSamples; idx++) {
			sampleData[idx] = readBytes(moduleData, sampleLengths[idx]);
			// No Soundtracker, se houver loop, os dados do loop são copiados para o início
			// da amostra.
			if (soundtracker && sampleLoopLength[idx] > 0) {
				byte[] data = sampleData[idx];
				int loopStartBytes = sampleLoopStart[idx] >> FIXED_POINT_SHIFT;
				int loopLengthBytes = sampleLoopLength[idx] >> FIXED_POINT_SHIFT;
				System.arraycopy(data, loopStartBytes, data, 0, loopLengthBytes);
				sampleLoopStart[idx] = 0; // O loop agora começa no início da amostra modificada.
			}
		}
		setSequencePos(0, 0); // Define a posição inicial da reprodução.
	}

	/**
	 * Escreve os dados do módulo para um OutputStream no formato Protracker.
	 *
	 * @param outputStream O OutputStream para escrever os dados do módulo.
	 * @throws java.io.IOException Se ocorrer um erro de I/O durante a escrita.
	 */
	public void writeModule(java.io.OutputStream outputStream) throws java.io.IOException {
		byte[] header = new byte[1084]; // Buffer para o cabeçalho do arquivo MOD.
		writeAscii(songName, header, 0, 20); // Escreve o nome da música.

		// Escreve informações de cada amostra no cabeçalho.
		for (int idx = 1; idx < MAX_SAMPLES; idx++) {
			writeAscii(instrumentNames[idx], header, idx * 30 - 10, 22);
			// Comprimento da amostra em palavras (2 bytes).
			int sampleLengthWords = sampleData[idx].length / 2;
			header[idx * 30 + 12] = (byte) (sampleLengthWords >> 8);
			header[idx * 30 + 13] = (byte) (sampleLengthWords & 0xFF);
			header[idx * 30 + 14] = (byte) sampleFineTune[idx];
			header[idx * 30 + 15] = (byte) sampleVolume[idx];

			// Início do loop da amostra em palavras.
			int loopStartBytes = sampleLoopStart[idx] >> FIXED_POINT_SHIFT;
			int loopStartWords = loopStartBytes / 2;
			header[idx * 30 + 16] = (byte) (loopStartWords >> 8);
			header[idx * 30 + 17] = (byte) (loopStartWords & 0xFF);

			// Comprimento do loop da amostra em palavras.
			int loopLengthBytes = sampleLoopLength[idx] >> FIXED_POINT_SHIFT;
			int loopLengthWords = loopLengthBytes / 2;
			header[idx * 30 + 18] = (byte) (loopLengthWords >> 8);
			header[idx * 30 + 19] = (byte) (loopLengthWords & 0xFF);
		}
		header[950] = (byte) songLength; // Escreve o comprimento da sequência.
		// Posição 127 do cabeçalho (byte de "restart position") é tipicamente 0x7F se
		// não usado, ou o índice do padrão de reinício.
		// Aqui, simplesmente copiamos a sequência, que pode incluir este valor.
		System.arraycopy(sequence, 0, header, 952, songLength); // Copia a sequência para o cabeçalho.

		// Determina o número máximo de padrões para escrever o identificador correto.
		int numPatterns = 0;
		for (int idx = 0; idx < songLength; idx++) {
			if (numPatterns <= sequence[idx]) {
				numPatterns = sequence[idx] + 1;
			}
		}

		// Escreve o identificador do tipo de módulo.
		if (numChannels == 4) {
			writeAscii(numPatterns > 64 ? "M!K!" : "M.K.", header, 1080, 4); // M!K! para mais de 64 padrões.
		} else {
			header[1080] = (byte) ('0' + numChannels); // Ex: '6'
			writeAscii("CHN", header, 1081, 3); // Ex: "CHN" -> "6CHN"
		}
		outputStream.write(header); // Escreve o cabeçalho no stream.

		// Converte e escreve os dados dos padrões.
		byte[] outputData = new byte[numChannels * 4 * 64 * numPatterns];
		for (int row = 0, rows = numPatterns * 64; row < rows; row++) {
			for (int chn = 0; chn < numChannels; chn++) {
				int patIdx = (row * patChannels + chn) * 4; // Índice no formato interno.
				int outIdx = (row * numChannels + chn) * 4; // Índice no formato de saída.
				int period = keyToPeriod(patternData[patIdx] & 0xFF, 0); // Converte nota para período (finetune 0 para
																																	// escrita).
				int instrument = patternData[patIdx + 1] & 0x1F; // Instrumento (0-31).
				// Formato Protracker: [Sample Hi Nibble + Period Hi Nibble], [Period Lo Byte],
				// [Sample Lo Nibble + Effect Nibble], [Effect Params]
				outputData[outIdx] = (byte) ((instrument & 0x10) | ((period >> 8) & 0xF));
				outputData[outIdx + 1] = (byte) (period & 0xFF);
				outputData[outIdx + 2] = (byte) (((instrument & 0xF) << 4) | (patternData[patIdx + 2] & 0xF));
				outputData[outIdx + 3] = patternData[patIdx + 3];
			}
		}
		outputStream.write(outputData); // Escreve os dados dos padrões.

		// Escreve os dados de cada amostra.
		for (int idx = 1; idx < MAX_SAMPLES; idx++) {
			// Garante que o comprimento da amostra seja par.
			outputStream.write(sampleData[idx], 0, sampleData[idx].length & -2);
		}
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String name) {
		songName = name.length() > 20 ? name.substring(0, 20) : name;
	}

	public String getInstrumentName(int idx) {
		return instrumentNames[idx];
	}

	public void setInstrumentName(int idx, String name) {
		instrumentNames[idx] = name.length() > 22 ? name.substring(0, 22) : name;
	}

	public int getSampleLength(int idx) {
		return sampleData[idx].length;
	}

	public int getSampleVolume(int idx) {
		return sampleVolume[idx];
	}

	public void setSampleVolume(int idx, int volume) {
		sampleVolume[idx] = (volume < 0 || volume > 64) ? 64 : volume;
	}

	public int getSampleFinetune(int idx) {
		int finetune = sampleFineTune[idx];
		// Converte de 0-15 para -8 a +7.
		return finetune < 8 ? finetune : finetune - 16;
	}

	public void setSampleFinetune(int idx, int finetune) {
		if (finetune < -8 || finetune > 7) {
			finetune = 0; // Valor padrão se fora do intervalo.
		}
		// Converte de -8 a +7 para 0-15.
		sampleFineTune[idx] = finetune < 0 ? finetune + 16 : finetune;
	}

	public int getSampleLoopStart(int idx) {
		return sampleLoopStart[idx] >> FIXED_POINT_SHIFT;
	}

	public int getSampleLoopLength(int idx) {
		return sampleLoopLength[idx] >> FIXED_POINT_SHIFT;
	}

	public void setSampleLoop(int idx, int loopStart, int loopLength) {
		int sampleLength = sampleData[idx].length;
		if (loopStart < 0 || loopStart > sampleLength) {
			loopStart = sampleLength; // Sem loop efetivo.
		}
		if (loopLength < 4 || loopStart + loopLength > sampleLength) {
			loopLength = sampleLength - loopStart; // Ajusta para caber na amostra.
		}
		// Garante que os valores sejam pares.
		sampleLoopStart[idx] = (loopStart & -2) * FIXED_POINT_ONE;
		sampleLoopLength[idx] = (loopLength & -2) * FIXED_POINT_ONE;
	}

	public byte[] getSampleData(int idx) {
		return sampleData[idx];
	}

	public void setSampleData(int idx, byte[] data) {
		// Limita o tamanho da amostra e garante que seja par.
		sampleData[idx] = new byte[(data.length > 0x1FFFE ? 0x1FFFE : data.length) & -2];
		System.arraycopy(data, 0, sampleData[idx], 0, sampleData[idx].length);
		// Define o loop para o final da amostra (sem loop efetivo) por padrão.
		setSampleLoop(idx, sampleData[idx].length, 0);
	}

	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
		// c2Rate é a frequência de C-2 em Hz, usada para calcular outras frequências.
		// Esses valores são padrões para Protracker.
		this.c2Rate = numChannels == 4 ? 8287 : 8363; // PAL: 8287 (4ch), 8363 (Amiga NTSC para >4ch)
	}

	public int getSongLength() {
		return songLength;
	}

	public int getPattern(int sequencePos) {
		return sequencePos < songLength ? sequence[sequencePos] : 0;
	}

	public void setSequence(byte[] sequence) {
		this.sequence = sequence;
		this.songLength = sequence.length;
	}

	public byte[] getPatternData() {
		return patternData;
	}

	public void setPatternData(byte[] patternData, int patChannels) {
		this.patternData = patternData;
		this.patChannels = patChannels;
		if (numChannels > patChannels) {
			// Ajusta o número de canais se for maior que o número de canais nos dados do
			// padrão.
			setNumChannels(patChannels);
		}
	}

	public int getRow() {
		return currentRow;
	}

	public int getSequencePos() {
		return currentSequencePos;
	}

	/**
	 * Define a posição na sequência. O tempo é resetado para o padrão.
	 *
	 * @param sequencePos A nova posição na sequência de padrões.
	 * @param row         A nova linha dentro do padrão.
	 */
	public void setSequencePos(int sequencePos, int row) {
		if (sequencePos < 0 || sequencePos >= songLength) {
			sequencePos = 0; // Posição padrão se inválida.
		}
		if (row < 0 || row > 63) {
			row = 0; // Linha padrão se inválida.
		}
		// Limpa todos os estados dos canais e variáveis de reprodução.
		clear(channelInstrument);
		clear(channelAssigned);
		clear(channelEffect);
		clear(channelParameter);
		clear(channelVolume);
		clear(channelPanning);
		clear(channelPeriod);
		clear(channelSamplePos);
		clear(channelFrequency);
		clear(channelArpeggio);
		clear(channelVibrato);
		clear(channelVibratoSpeed);
		clear(channelVibratoDepth);
		clear(channelVibratoPhase);
		clear(channelPortaPeriod);
		clear(channelPortaSpeed);
		clear(channelTremolo);
		clear(channelTremoloSpeed);
		clear(channelTremoloDepth);
		clear(channelPatternLoopRow);
		clear(channelSampleOffset);

		// Define a posição atual e próxima da sequência/linha.
		currentSequencePos = nextSequencePos = sequencePos;
		currentRow = nextRow = row;
		// Reseta o tick, ticks por linha, tempo e contadores de efeito/loop.
		currentTick = 0;
		ticksPerRow = 6; // Velocidade padrão.
		tempo = 125; // Tempo padrão (BPM).
		effectCounter = 0;
		patternLoopCount = 0;
		patternLoopChannel = 0;
		clear(rampBuf); // Limpa o buffer de rampa de volume.

		// Define o panning padrão para os canais (configuração estéreo LRLR...).
		for (int chn = 0; chn < MAX_CHANNELS; chn += 4) {
			channelPanning[chn] = channelPanning[chn + 3] = FIXED_POINT_ONE / 5; // ~20% para canais externos
			channelPanning[chn + 1] = channelPanning[chn + 2] = FIXED_POINT_ONE * 4 / 5; // ~80% para canais internos
		}
		tick(); // Processa o primeiro tick para inicializar o estado dos efeitos e frequências.
	}

	/**
	 * Avança para a posição especificada na sequência, simulando a reprodução.
	 *
	 * @param sequencePos A posição da sequência desejada.
	 * @param row         A linha desejada dentro do padrão.
	 * @param sampleRate  A taxa de amostragem usada para calcular o avanço.
	 */
	public void seek(int sequencePos, int row, int sampleRate) {
		if (sequencePos < 0 || sequencePos >= songLength) {
			sequencePos = 0;
		}
		if (row < 0 || row > 63) {
			row = 0;
		}
		setSequencePos(0, 0); // Reseta para o início.
		// Loop até alcançar a posição desejada.
		while (currentSequencePos < sequencePos || currentRow < row) {
			// Calcula o número de amostras de áudio que seriam geradas em um tick.
			int count = (sampleRate * 5) / (tempo * 2); // (sampleRate / (tempo / 2.5)) / ticksPerRow (assumindo
																									// ticksPerRow=6)
			for (int chn = 0; chn < numChannels; chn++) {
				// Atualiza a posição da amostra para cada canal (simula a reprodução sem gerar
				// áudio).
				updateSamplePos(chn, count, sampleRate);
			}
			boolean songEnd = tick(); // Processa um tick do sequenciador.
			if (songEnd) {
				// Se o fim da música for alcançado antes da posição desejada, define a posição
				// diretamente.
				setSequencePos(sequencePos, row);
				return;
			}
		}
	}

	public int getMute() {
		return mute;
	}

	public void setMute(int bitmask) {
		mute = bitmask;
	}

	public boolean getSequencer() {
		return sequencerEnabled;
	}

	public void setSequencer(boolean enabled) {
		sequencerEnabled = enabled;
	}

	/**
	 * Dispara (inicia) uma nota em um canal específico.
	 * Usado para interação externa, não pelo sequenciador principal.
	 *
	 * @param channel    O canal para disparar a nota.
	 * @param instrument O índice do instrumento a ser usado.
	 * @param key        A nota a ser tocada (0-72).
	 * @param volume     O volume da nota (0-64).
	 */
	public void trigger(int channel, int instrument, int key, int volume) {
		if (instrument > 0 && instrument < MAX_SAMPLES) { // Verifica se o instrumento é válido.
			channelInstrument[channel] = instrument; // Define o instrumento do canal.
			int period = keyToPeriod(key, sampleFineTune[instrument]); // Calcula o período da nota.
			// Calcula a frequência de reprodução. Se o período for 0 (nota inválida), usa
			// c2Rate.
			channelFrequency[channel] = period > 0 ? c2Rate * 428 / period : c2Rate;
			channelSamplePos[channel] = 0; // Reseta a posição da amostra para o início.
		}
		channelVolume[channel] = volume; // Define o volume do canal.
	}

	/**
	 * Gera um bloco de áudio estéreo.
	 * Retorna o número de amostras estéreo produzidas. O buffer de saída deve ter o
	 * comprimento de sampleRate / 50 * 2 (para estéreo).
	 * Tipicamente, para 50Hz, isso é sampleRate / 25 amostras mono, ou sampleRate /
	 * 50 pares estéreo.
	 * O cálculo (sampleRate * 5) / (tempo * 2) resulta em (sampleRate / (tempo /
	 * 2.5)) amostras.
	 * Se tempo = 125 BPM, então (tempo / 2.5) = 50 (ticks por segundo).
	 *
	 * @param sampleRate A taxa de amostragem da saída de áudio.
	 * @param output     O buffer para armazenar as amostras de áudio geradas
	 *                   (intercaladas L/R).
	 * @return O número de amostras estéreo (pares L/R) geradas.
	 */
	public int getAudio(int sampleRate, int[] output) {
		// Calcula o número de amostras de áudio a serem geradas para este tick.
		// (sampleRate * 5) / (tempo * 2) = sampleRate / (tempo / 2.5)
		// Se tempo = 125 BPM, (tempo / 2.5) = 50. Então, count = sampleRate / 50.
		// Isso corresponde a 1/50 de segundo de áudio, ou um "tick" a 50Hz.
		int count = (sampleRate * 5) / (tempo * 2);

		// Limpa o buffer de saída (incluindo a parte para o crossfade/ramping).
		// O +32 é para a rampa de volume (volumeRamp).
		for (int idx = 0, end = (count + 32) * 2; idx < end; idx++) {
			output[idx] = 0;
		}

		// Para cada canal:
		for (int chn = 0; chn < numChannels; chn++) {
			if ((mute & (1 << chn)) == 0) { // Se o canal não estiver silenciado:
				// Gera o áudio do canal e adiciona ao buffer de saída.
				// O +32 é para garantir que haja dados suficientes para a interpolação da rampa
				// de volume.
				resample(chn, output, count + 32, sampleRate);
			}
			// Atualiza a posição da amostra para o próximo tick, mesmo se silenciado.
			updateSamplePos(chn, count, sampleRate);
		}

		volumeRamp(output, count); // Aplica suavização de volume (crossfade) para evitar cliques.

		if (sequencerEnabled) { // Se o sequenciador estiver habilitado:
			tick(); // Avança o estado do sequenciador para o próximo tick.
		}
		return count; // Retorna o número de amostras estéreo geradas.
	}

	/**
	 * Atualiza a posição de reprodução da amostra para um canal, sem gerar áudio.
	 * Usado por `seek` e para manter o estado da amostra em `getAudio`.
	 *
	 * @param channel    O canal a ser atualizado.
	 * @param count      O número de amostras de saída que teriam sido geradas.
	 * @param sampleRate A taxa de amostragem da saída.
	 */
	private void updateSamplePos(int channel, int count, int sampleRate) {
		int instrument = channelInstrument[channel];
		if (instrument == 0)
			return; // Sem instrumento, sem atualização.

		int loopStart = sampleLoopStart[instrument];
		int loopLength = sampleLoopLength[instrument];
		// Calcula o incremento da posição da amostra por amostra de saída (em ponto
		// fixo).
		int step = channelFrequency[channel] * FIXED_POINT_ONE / sampleRate;
		// Calcula a nova posição da amostra após 'count' amostras de saída.
		int samplePos = channelSamplePos[channel] + step * count;

		// Verifica se a posição ultrapassou o fim do loop (ou da amostra se não houver
		// loop).
		if (samplePos >= loopStart + loopLength) {
			if (loopLength > 0) { // Se houver loop, ajusta a posição para dentro do loop.
				samplePos = loopStart + (samplePos - loopStart) % loopLength;
			} else { // Se não houver loop, fixa a posição no início do loop (que seria o fim da
								// amostra).
				samplePos = loopStart; // Ou poderia ser loopStart + loopLength -1 para parar no último sample.
				// Comportamento de MOD é parar de tocar ou tocar o último sample.
				// Aqui, ele vai continuar lendo do loopStart, que se loopLength=0, é o fim.
				// `resample` trata isso parando a saída se loopLength <= 0.
			}
		}
		channelSamplePos[channel] = samplePos; // Armazena a nova posição da amostra.
	}

	/**
	 * Gera áudio para um canal (resampling por vizinho mais próximo) e o mistura no
	 * buffer de saída.
	 *
	 * @param channel    O canal para gerar áudio.
	 * @param output     O buffer de saída (intercalado L/R).
	 * @param count      O número de amostras de saída a gerar (inclui amostras
	 *                   extras para rampa).
	 * @param sampleRate A taxa de amostragem da saída.
	 */
	private void resample(int channel, int[] output, int count, int sampleRate) {
		int instrument = channelInstrument[channel];
		if (instrument == 0 || sampleData[instrument].length == 0)
			return; // Sem instrumento ou amostra vazia.

		int loopStart = sampleLoopStart[instrument];
		int loopLength = sampleLoopLength[instrument];
		int loopEnd = loopStart + loopLength;
		int samplePos = channelSamplePos[channel]; // Posição atual na amostra (ponto fixo).
		// Incremento da posição da amostra por amostra de saída (ponto fixo).
		int step = channelFrequency[channel] * FIXED_POINT_ONE / sampleRate;
		int volume = channelVolume[channel]; // Volume atual do canal.
		int panning = channelPanning[channel]; // Panning atual do canal (ponto fixo).

		// Para cada amostra de saída a ser gerada:
		for (int idx = 0, end = count * 2; idx < end; idx += 2) { // idx avança de 2 em 2 (estéreo).
			if (samplePos >= loopEnd) { // Se a posição da amostra atingir o fim do loop/amostra:
				if (loopLength <= 0) { // Se não houver loop, para de gerar áudio para este canal.
					return;
				}
				// Envolve a posição da amostra de volta ao início do loop.
				while (samplePos >= loopEnd) {
					samplePos -= loopLength;
				}
			}
			// Obtém o valor da amostra (8-bit signed) e aplica o volume.
			// samplePos >> FIXED_POINT_SHIFT converte de ponto fixo para índice do array.
			int amplitude = sampleData[instrument][samplePos >> FIXED_POINT_SHIFT] * volume;
			// Aplica panning e adiciona ao canal esquerdo.
			output[idx] += (amplitude * (FIXED_POINT_ONE - panning)) >> FIXED_POINT_SHIFT;
			// Aplica panning e adiciona ao canal direito.
			output[idx + 1] += (amplitude * panning) >> FIXED_POINT_SHIFT;
			samplePos += step; // Avança a posição na amostra do instrumento.
		}
	}

	/**
	 * Aplica um crossfade suave entre o final do buffer anterior e o início do
	 * buffer atual
	 * para evitar cliques de descontinuidade.
	 *
	 * @param output O buffer de áudio atual.
	 * @param count  O número de amostras estéreo no buffer atual (sem as extras
	 *               para rampa).
	 */
	private void volumeRamp(int[] output, int count) {
		// Para as primeiras 32 amostras do buffer atual:
		for (int idx = 0; idx < 32; idx++) {
			// Interpola linearmente o canal esquerdo.
			output[idx * 2] = (output[idx * 2] * idx + rampBuf[idx * 2] * (32 - idx)) / 32;
			// Armazena a amostra correspondente do final do buffer atual para o próximo
			// crossfade.
			rampBuf[idx * 2] = output[(count + idx) * 2];
			// Interpola linearmente o canal direito.
			output[idx * 2 + 1] = (output[idx * 2 + 1] * idx + rampBuf[idx * 2 + 1] * (32 - idx)) / 32;
			// Armazena a amostra correspondente do final do buffer atual para o próximo
			// crossfade.
			rampBuf[idx * 2 + 1] = output[(count + idx) * 2 + 1];
		}
	}

	/**
	 * Processa uma nova linha (row) no padrão atual.
	 * Lê notas, instrumentos e efeitos da linha e atualiza o estado dos canais.
	 *
	 * @return true se o fim da música foi alcançado, false caso contrário.
	 */
	private boolean row() {
		// Verifica se a música terminou (loopou ou alcançou o fim da sequência).
		boolean songEnd = nextSequencePos < currentSequencePos
				|| (nextSequencePos == currentSequencePos && nextRow <= currentRow && patternLoopCount <= 0);
		boolean patternBreak = false; // Flag para indicar se ocorreu um salto de padrão (efeito Bxx ou Dxx).

		// Atualiza currentSequencePos.
		if (nextSequencePos < songLength) {
			currentSequencePos = nextSequencePos;
		} else { // Fim da sequência, volta ao início.
			currentSequencePos = nextSequencePos = 0;
			songEnd = true;
		}

		// Atualiza currentRow.
		if (nextRow < 64) {
			currentRow = nextRow;
		} else { // Fim do padrão, reseta a linha.
			currentRow = 0; // Deveria ser tratado pelo nextRow > 63 abaixo.
		}

		currentTick = ticksPerRow; // Reseta o contador de ticks para a nova linha.
		nextRow = currentRow + 1; // Prepara para a próxima linha.
		if (nextRow > 63) { // Se a próxima linha ultrapassar 63:
			nextRow = 0; // Volta para a primeira linha.
			nextSequencePos = currentSequencePos + 1; // Avança para o próximo padrão na sequência.
			patternBreak = true; // Indica que houve uma quebra de padrão natural.
		}

		// Para cada canal:
		for (int chn = 0; chn < numChannels; chn++) {
			// Calcula o offset para os dados do padrão desta linha e canal.
			int rowOffset = sequence[currentSequencePos] * 64 + currentRow;
			int patternDataOffset = (rowOffset * patChannels + chn) * 4;
			// Lê nota, instrumento, efeito e parâmetro do padrão.
			int key = patternData[patternDataOffset] & 0xFF;
			int instrument = patternData[patternDataOffset + 1] & 0xFF;
			int effect = patternData[patternDataOffset + 2] & 0xF;
			int parameter = patternData[patternDataOffset + 3] & 0xFF;

			// Mapeia efeitos Exx para um código interno (0xE0 | nibble).
			if (effect == 0xE) {
				effect = 0xE0 | ((parameter >> 4) & 0xF);
				parameter = parameter & 0xF;
			}

			// Se um instrumento é especificado:
			if (instrument > 0) {
				channelAssigned[chn] = instrument; // Armazena o instrumento "designado" para esta linha.
				channelVolume[chn] = sampleVolume[instrument]; // Define o volume do canal para o volume padrão do instrumento.
				channelSampleOffset[chn] = 0; // Reseta o offset da amostra.
				// Se o instrumento atual já estava tocando e tem loop, permite que continue
				// (para portamento).
				// Isso evita que o instrumento seja "retriggerado" se a mesma amostra for usada
				// com portamento.
				if (sampleLoopLength[instrument] > 0 && channelInstrument[chn] > 0) {
					// Não muda channelInstrument[chn] aqui, para permitir que efeitos como
					// portamento continuem.
					// A mudança de channelInstrument[chn] ocorre se uma nota for tocada sem
					// portamento.
				}
			}

			// Efeito 9xx (Sample Offset): define o deslocamento inicial da amostra.
			if (effect == 0x9) {
				channelSampleOffset[chn] = parameter * 256 * FIXED_POINT_ONE;
			}

			// Se uma nota é especificada:
			if (key > 0) {
				channelPortaPeriod[chn] = keyToPeriod(key, sampleFineTune[channelAssigned[chn]]); // Define o período alvo para
																																													// portamento.
				// Se não for um efeito de portamento (3xx, 5xx) ou delay de nota (EDx com
				// parâmetro > 0), dispara a nota imediatamente.
				if (effect != 0x3 && effect != 0x5 && !(effect == 0xED && parameter > 0)) {
					channelInstrument[chn] = channelAssigned[chn]; // Define o instrumento do canal.
					channelPeriod[chn] = channelPortaPeriod[chn]; // Define o período do canal.
					channelSamplePos[chn] = channelSampleOffset[chn]; // Define a posição inicial da amostra (considerando efeito
																														// 9xx).
					channelVibratoPhase[chn] = 0; // Reseta a fase do vibrato.
				}
			}

			// Armazena o efeito e parâmetro para processamento no tick().
			channelEffect[chn] = effect;
			channelParameter[chn] = parameter;
			// Reseta arpejo, vibrato e tremolo para esta linha (serão recalculados no tick
			// se necessário).
			channelArpeggio[chn] = 0;
			channelVibrato[chn] = 0;
			channelTremolo[chn] = 0;

			// Processa efeitos que são aplicados no início da linha (tick 0).
			switch (effect) {
				case 0x0: /* Arpeggio. */ // Processado no tick().
				case 0x1: /* Portamento up. */ // Processado no tick().
				case 0x2: /* Portamento down. */ // Processado no tick().
					break;
				case 0x3: /* Tone portamento. */
					if (parameter > 0) {
						channelPortaSpeed[chn] = parameter; // Define a velocidade do portamento.
					}
					break;
				case 0x4: /* Vibrato. */
					if ((parameter & 0xF0) > 0) { // Nibble superior define a velocidade.
						channelVibratoSpeed[chn] = (parameter & 0xF0) >> 4;
					}
					if ((parameter & 0xF) > 0) { // Nibble inferior define a profundidade.
						channelVibratoDepth[chn] = parameter & 0xF;
					}
					vibrato(chn); // Aplica o primeiro passo do vibrato.
					break;
				case 0x5: /* Tone porta + Volume slide. */ // Volume slide é processado no tick().
					break;
				case 0x6: /* Vibrato + Volume slide. */ // Volume slide é processado no tick().
					vibrato(chn); // Aplica o primeiro passo do vibrato.
					break;
				case 0x7: /* Tremolo. */
					if ((parameter & 0xF0) > 0) { // Nibble superior define a velocidade.
						channelTremoloSpeed[chn] = (parameter & 0xF0) >> 4;
					}
					if ((parameter & 0xF) > 0) { // Nibble inferior define a profundidade.
						channelTremoloDepth[chn] = parameter & 0xF;
					}
					tremolo(chn); // Aplica o primeiro passo do tremolo.
					break;
				case 0x8: /* Set panning. */
					if (numChannels > 4) { // Panning só funciona em módulos com mais de 4 canais no Protracker original.
						if (parameter > 128) { // Parâmetro > 128 (0x80) é geralmente surround (não implementado aqui,
																		// centraliza).
							channelPanning[chn] = FIXED_POINT_ONE / 2; // Centro.
						} else {
							channelPanning[chn] = parameter * FIXED_POINT_ONE / 128; // 0x00 (esquerda) a 0x80 (direita).
						}
					}
					break;
				case 0x9: /* Set sample offset. */ // Já tratado acima.
					break;
				case 0xA: /* Volume slide. */ // Processado no tick().
					break;
				case 0xB: /* Pattern jump. */
					nextSequencePos = parameter; // Define a próxima posição na sequência.
					if (!patternBreak) { // Se não houve uma quebra de padrão natural.
						nextRow = 0; // Salta para o início do novo padrão.
						patternBreak = true;
					}
					break;
				case 0xC: /* Set volume. */
					channelVolume[chn] = parameter > 64 ? 64 : parameter; // Define o volume do canal (0-64).
					break;
				case 0xD: /* Pattern break. */
					if (!patternBreak) { // Se não houve uma quebra de padrão natural.
						nextSequencePos = currentSequencePos + 1; // Avança para o próximo padrão.
						patternBreak = true;
					}
					// Define a próxima linha no novo padrão (BCD: dezena*10 + unidade).
					nextRow = (parameter >> 4) * 10 + (parameter & 0xF);
					if (nextRow > 63)
						nextRow = 0; // Garante que a linha seja válida.
					break;
				case 0xE: /* Remapped to 0xEx. */ // Não deveria acontecer aqui, já foi remapeado.
					break;
				case 0xF: /* Set speed/tempo. */
					if (parameter > 31) { // Parâmetro > 31 (0x1F) define o tempo (BPM).
						tempo = parameter;
					} else if (parameter > 0) { // Parâmetro 0-31 (0x00-0x1F) define a velocidade (ticks por linha).
						currentTick = ticksPerRow = parameter;
					}
					break;
				// Efeitos Estendidos (Exx)
				case 0xE0: /* Set filter. */ // Não implementado.
					break;
				case 0xE1: /* Fine portamento up. */
					channelPeriod[chn] -= parameter; // Ajusta o período para cima (diminui).
					break;
				case 0xE2: /* Fine portamento down. */
					channelPeriod[chn] += parameter; // Ajusta o período para baixo (aumenta).
					break;
				case 0xE3: /* Glissando. */ // Não implementado (geralmente controla se portamento usa semitons).
				case 0xE4: /* Set vibrato waveform. */ // Não implementado (Protracker só tem senoide).
				case 0xE5: /* Set finetune. */ // Não implementado em tempo real (afeta o instrumento).
					break;
				case 0xE6: /* Pattern loop. */
					if (channelPatternLoopRow[chn] < currentRow) { // Garante que o loop não comece na mesma linha.
						if (parameter > 0) { // Parâmetro > 0 define o número de loops.
							if (patternLoopCount <= 0) { // Inicia um novo loop.
								patternLoopCount = parameter + 1; // +1 porque conta a primeira passagem.
								patternLoopChannel = chn; // Canal que iniciou o loop.
							}
							if (patternLoopChannel == chn) { // Apenas o canal que iniciou o loop o controla.
								patternLoopCount--;
								if (patternLoopCount > 0) { // Se ainda houver loops.
									nextSequencePos = currentSequencePos; // Mantém o padrão atual.
									nextRow = channelPatternLoopRow[chn]; // Volta para a linha de início do loop.
									patternBreak = false; // Cancela qualquer quebra de padrão.
								} else { // Fim do loop.
									channelPatternLoopRow[chn] = currentRow + 1; // Evita re-loop imediato.
								}
							}
						} else { // Parâmetro == 0 define o ponto de início do loop.
							channelPatternLoopRow[chn] = currentRow;
						}
					}
					break;
				case 0xE7: /* Set tremolo waveform. */ // Não implementado.
				case 0xE8: /* Panning. */ // Não implementado (Protracker usa efeito 8xx).
				case 0xE9: /* Retrig. */ // Processado no tick().
					break;
				case 0xEA: /* Fine volume slide up. */
					channelVolume[chn] += parameter;
					break;
				case 0xEB: /* Fine volume slide down. */
					channelVolume[chn] -= parameter;
					break;
				case 0xEC: /* Note cut. */ // Processado no tick().
					break;
				case 0xED: /* Note delay. */
					if (key <= 0) { // Se não houver nota, o efeito é ignorado.
						channelParameter[chn] = 0; // Zera o parâmetro para não disparar no tick().
					}
					// A nota é disparada no tick() quando effectCounter == parameter.
					break;
				case 0xEE: /* Pattern delay. */
					currentTick += ticksPerRow * parameter; // Adiciona ticks extras à linha atual.
					break;
				case 0xEF: /* Invert loop. */ // Não implementado (efeito de Amiga específico).
					break;
			}
		}
		// Se ocorreu um salto de padrão (Bxx, Dxx ou fim do padrão), reseta os
		// contadores de loop de padrão.
		if (patternBreak) {
			patternLoopCount = 0;
			patternLoopChannel = 0;
			for (int chn = 0; chn < numChannels; chn++) {
				channelPatternLoopRow[chn] = 0; // Reseta o ponto de início do loop para todos os canais.
			}
		}
		return songEnd;
	}

	/**
	 * Processa um tick do sequenciador. Chamado `ticksPerRow` vezes por linha.
	 * Atualiza efeitos contínuos e calcula a frequência final para cada canal.
	 *
	 * @return true se o fim da música foi alcançado (após processar uma nova
	 *         linha), false caso contrário.
	 */
	private boolean tick() {
		boolean songEnd = false;
		if (--currentTick <= 0) { // Se todos os ticks da linha foram processados:
			songEnd = row(); // Avança para a próxima linha e processa seus eventos.
			effectCounter = 0; // Reseta o contador de efeitos para a nova linha.
		} else { // Se ainda há ticks na linha atual:
			effectCounter++; // Incrementa o contador de efeitos.
			// Para cada canal:
			for (int chn = 0; chn < numChannels; chn++) {
				// Processa efeitos que são atualizados a cada tick.
				switch (channelEffect[chn]) {
					case 0x0: /* Arpeggio. */
						if (channelParameter[chn] > 0) {
							switch (effectCounter % 3) { // Ciclo de 3 ticks para arpejo.
								default: // Tick 0: nota base.
									channelArpeggio[chn] = 0;
									break;
								case 1: // Tick 1: nota base + semitons do nibble superior.
									channelArpeggio[chn] = (channelParameter[chn] >> 4) & 0xF;
									break;
								case 2: // Tick 2: nota base + semitons do nibble inferior.
									channelArpeggio[chn] = channelParameter[chn] & 0xF;
									break;
							}
						}
						break;
					case 0x1: /* Portamento up. */
						channelPeriod[chn] -= channelParameter[chn]; // Diminui o período (aumenta a frequência).
						break;
					case 0x2: /* Portamento down. */
						channelPeriod[chn] += channelParameter[chn]; // Aumenta o período (diminui a frequência).
						break;
					case 0x3: /* Tone portamento. */
						tonePortamento(chn); // Aplica o slide de tom.
						break;
					case 0x4: /* Vibrato. */
						vibrato(chn); // Aplica o vibrato.
						break;
					case 0x5: /* Tone porta + Volume slide. */
						tonePortamento(chn);
						volumeSlide(chn);
						break;
					case 0x6: /* Vibrato + Volume slide. */
						vibrato(chn);
						volumeSlide(chn);
						break;
					case 0x7: /* Tremolo. */
						tremolo(chn); // Aplica o tremolo.
						break;
					case 0xA: /* Volume slide. */
						volumeSlide(chn); // Aplica o slide de volume.
						break;
					// Efeitos Estendidos (Exx)
					case 0xE9: /* Retrig. */
						if (channelParameter[chn] > 0 && effectCounter % channelParameter[chn] == 0) {
							channelSamplePos[chn] = 0; // Reinicia a amostra.
						}
						break;
					case 0xEC: /* Note cut. */
						if (effectCounter == channelParameter[chn]) {
							channelVolume[chn] = 0; // Zera o volume no tick especificado.
						}
						break;
					case 0xED: /* Note delay. */
						if (effectCounter == channelParameter[chn]) {
							// Dispara a nota no tick especificado.
							channelInstrument[chn] = channelAssigned[chn];
							channelPeriod[chn] = channelPortaPeriod[chn];
							channelSamplePos[chn] = channelSampleOffset[chn];
							channelVibratoPhase[chn] = 0;
						}
						break;
				}
			}
		}

		// Cálculos finais de volume e frequência para cada canal, após todos os efeitos
		// do tick.
		for (int chn = 0; chn < numChannels; chn++) {
			// Calcula o volume final (aplicando tremolo e limitando).
			int volume = channelVolume[chn];
			if (volume > 64) {
				volume = channelVolume[chn] = 64;
			} else if (volume < 0) {
				volume = channelVolume[chn] = 0;
			}
			volume = volume + channelTremolo[chn]; // Aplica o efeito de tremolo.
			if (volume > 64) {
				volume = 64;
			} else if (volume < 0) {
				volume = 0;
			}
			// channelVolume[chn] é atualizado aqui para refletir o volume base antes do
			// tremolo,
			// mas o 'volume' usado para resample inclui o tremolo.
			// No entanto, o código original parece usar channelVolume[chn] diretamente no
			// resample,
			// e o tremolo é aplicado a channelTremolo[chn] que é somado depois.
			// Para consistência com o original, o volume final para resample será calculado
			// lá.
			// Aqui, apenas garantimos que channelVolume[chn] (base) esteja nos limites.

			// Calcula o período final (aplicando vibrato e arpejo).
			int period = channelPeriod[chn];
			if (period > 0) {
				// Aplica vibrato e depois arpejo ao período.
				period = transpose(period + channelVibrato[chn], channelArpeggio[chn]);
				if (period < 28) { // Limita o período mínimo para evitar frequências muito altas/inválidas.
					// O período mais baixo em KEY_TO_PERIOD é 26.
					period = 6848; // Período muito alto (nota muito baixa) para silenciar efetivamente.
				}
				// Converte período para frequência. 428 é um fator de conversão relacionado a
				// C-2 e KEY_TO_PERIOD.
				channelFrequency[chn] = c2Rate * 428 / period;
			}
		}
		return songEnd;
	}

	/**
	 * Aplica o efeito de portamento de tom (slide entre notas).
	 *
	 * @param chn O canal ao qual aplicar o efeito.
	 */
	private void tonePortamento(int chn) {
		if (channelPeriod[chn] < channelPortaPeriod[chn]) { // Se o período atual for menor que o alvo (nota mais alta).
			channelPeriod[chn] += channelPortaSpeed[chn]; // Aumenta o período (desliza para baixo).
			if (channelPeriod[chn] > channelPortaPeriod[chn]) { // Se ultrapassar o alvo.
				channelPeriod[chn] = channelPortaPeriod[chn]; // Fixa no alvo.
			}
		} else { // Se o período atual for maior que o alvo (nota mais baixa).
			channelPeriod[chn] -= channelPortaSpeed[chn]; // Diminui o período (desliza para cima).
			if (channelPeriod[chn] < channelPortaPeriod[chn]) { // Se ultrapassar o alvo.
				channelPeriod[chn] = channelPortaPeriod[chn]; // Fixa no alvo.
			}
		}
	}

	/**
	 * Aplica o efeito de vibrato (modulação de frequência).
	 *
	 * @param chn O canal ao qual aplicar o efeito.
	 */
	private void vibrato(int chn) {
		int phase = channelVibratoPhase[chn] & 0x3F; // Fase do vibrato (0-63).
		// Usa a tabela VIBRATO (meio ciclo) e a profundidade para calcular o desvio do
		// período.
		channelVibrato[chn] = (VIBRATO[phase & 0x1F] * channelVibratoDepth[chn]) >> 7;
		if (phase > 0x1F) { // Segunda metade do ciclo (inverte o sinal).
			channelVibrato[chn] = -channelVibrato[chn];
		}
		channelVibratoPhase[chn] += channelVibratoSpeed[chn]; // Avança a fase.
	}

	/**
	 * Aplica o efeito de slide de volume.
	 *
	 * @param chn O canal ao qual aplicar o efeito.
	 */
	private void volumeSlide(int chn) {
		int up = (channelParameter[chn] >> 4) & 0xF; // Nibble superior: slide para cima.
		int down = channelParameter[chn] & 0xF; // Nibble inferior: slide para baixo.
		channelVolume[chn] += up - down; // Ajusta o volume.
		// Limites de volume (0-64) são aplicados no final do tick().
	}

	/**
	 * Aplica o efeito de tremolo (modulação de amplitude/volume).
	 *
	 * @param chn O canal ao qual aplicar o efeito.
	 */
	private void tremolo(int chn) {
		int phase = channelVibratoPhase[chn] & 0x3F; // Reutiliza channelVibratoPhase para tremolo.
		// Usa a tabela VIBRATO e a profundidade para calcular o desvio do volume.
		// O shift é >> 6 para tremolo, >> 7 para vibrato, resultando em maior amplitude
		// para tremolo.
		channelTremolo[chn] = (VIBRATO[phase & 0x1F] * channelTremoloDepth[chn]) >> 6;
		if (phase > 0x1F) { // Segunda metade do ciclo (inverte o sinal).
			channelTremolo[chn] = -channelTremolo[chn];
		}
		channelVibratoPhase[chn] += channelTremoloSpeed[chn]; // Avança a fase (usando a velocidade do tremolo).
	}

	/**
	 * Converte uma nota (key) e ajuste fino para um valor de período Protracker.
	 *
	 * @param key      A nota (0-72, onde 1 = C-0, ..., 13 = C-1, etc.).
	 * @param fineTune O ajuste fino (0-15, onde 8-15 são negativos -8 a -1).
	 * @return O período Protracker correspondente.
	 */
	private static int keyToPeriod(int key, int fineTune) {
		int period = 0;
		if (key > 0 && key < 73) { // Notas válidas.
			// (KEY_TO_PERIOD[key] * FINE_TUNE[fineTune & 0xF]) é uma multiplicação de 16.16
			// bits.
			// >> (FIXED_POINT_SHIFT - 1) ajusta para a escala correta.
			// O -1 é porque FINE_TUNE já tem um fator de 2 implícito para o arredondamento
			// final.
			period = (KEY_TO_PERIOD[key] * FINE_TUNE[fineTune & 0xF]) >> (FIXED_POINT_SHIFT - 1);
		}
		// Arredondamento final para o período.
		return (period >> 1) + (period & 1);
	}

	/**
	 * Converte um valor de período Protracker de volta para a nota mais próxima.
	 *
	 * @param period O período Protracker.
	 * @return A nota (key) correspondente (0-72).
	 */
	private static int periodToKey(int period) {
		int key = 0;
		// Verifica se o período está dentro do intervalo conhecido.
		if (period >= KEY_TO_PERIOD[72] && period <= KEY_TO_PERIOD[1]) {
			// Encontra a oitava correta.
			while (KEY_TO_PERIOD[key + 12] > period) {
				key += 12;
			}
			// Encontra a nota correta dentro da oitava.
			while (KEY_TO_PERIOD[key + 1] >= period) {
				key++;
			}
			// Ajusta para a nota mais próxima se estiver entre duas.
			if ((KEY_TO_PERIOD[key] - period) >= (period - KEY_TO_PERIOD[key + 1])) {
				key++;
			}
		}
		return key;
	}

	/**
	 * Transpõe um período por um número de semitons.
	 *
	 * @param period    O período original.
	 * @param semitones O número de semitons para transpor (positivo ou negativo).
	 * @return O novo período transposto.
	 */
	private static int transpose(int period, int semitones) {
		// KEY_TO_PERIOD[13] é C-1 (856). A fórmula é baseada na relação de frequências.
		// Multiplica por KEY_TO_PERIOD[semitones + 13] e divide por KEY_TO_PERIOD[C-1
		// (856)].
		// O fator 2 é para manter precisão antes do arredondamento.
		period = period * KEY_TO_PERIOD[semitones + 13] * 2 / 856;
		return (period >> 1) + (period & 1); // Arredondamento.
	}

	/**
	 * Preenche um array de inteiros com zeros.
	 *
	 * @param array O array a ser limpo.
	 */
	private static void clear(int[] array) {
		java.util.Arrays.fill(array, 0);
	}

	/**
	 * Escreve uma string ASCII em um buffer de bytes, preenchendo com espaços.
	 *
	 * @param text   A string a ser escrita.
	 * @param outBuf O buffer de bytes de saída.
	 * @param offset O deslocamento no buffer onde começar a escrever.
	 * @param len    O número de bytes a escrever (preenchendo com espaços se
	 *               necessário).
	 */
	private static void writeAscii(String text, byte[] outBuf, int offset, int len) {
		for (int idx = 0; idx < len; idx++) {
			outBuf[offset + idx] = (byte) (idx < text.length() ? text.charAt(idx) : ' '); // Usar literal de caractere para
																																										// espaço.
		}
	}

	/**
	 * Lê uma string de um InputStream, tratando caracteres de controle e usando
	 * ISO-8859-1.
	 *
	 * @param inputStream  O InputStream de onde ler.
	 * @param bufferLength O número de bytes a ler para a string.
	 * @return A string lida.
	 * @throws java.io.IOException Se ocorrer um erro de I/O.
	 */
	public static String readString(java.io.InputStream inputStream, int bufferLength) throws java.io.IOException {
		byte[] bytes = readBytes(inputStream, bufferLength);
		int actualStringLength = 0;
		for (int idx = 0; idx < bytes.length; idx++) {
			if ((bytes[idx] & 0xFF) <= 32) { // Caracteres de controle ou espaço.
				bytes[idx] = ' '; // Usar literal de caractere para espaço.
			} else {
				actualStringLength = idx + 1; // Atualiza o comprimento real da string (sem espaços finais).
			}
		}
		return new String(bytes, 0, actualStringLength, java.nio.charset.StandardCharsets.ISO_8859_1);
	}

	/**
	 * Lê um número especificado de bytes de um InputStream.
	 *
	 * @param inputStream O InputStream de onde ler.
	 * @param length      O número de bytes a ler.
	 * @return Um array de bytes contendo os dados lidos.
	 * @throws java.io.IOException Se ocorrer um erro de I/O ou se o fim do stream
	 *                             for alcançado prematuramente.
	 */
	public static byte[] readBytes(java.io.InputStream inputStream, int length) throws java.io.IOException {
		byte[] bytes = new byte[length];
		int offset = 0;
		int count = 0;
		while (offset < length && count >= 0) {
			offset += count;
			count = inputStream.read(bytes, offset, length - offset);
		}
		if (offset < length) {
			throw new java.io.EOFException("Fim inesperado do stream ao ler bytes.");
		}
		return bytes;
	}

	/**
	 * Preenche uma string com um caractere até um certo comprimento, à esquerda ou
	 * à direita.
	 *
	 * @param string A string original.
	 * @param chr    O caractere de preenchimento.
	 * @param length O comprimento desejado da string final.
	 * @param left   true para preencher à esquerda, false para preencher à direita.
	 * @return A string preenchida.
	 */
	public static String pad(String string, char chr, int length, boolean left) {
		if (string.length() < length) {
			char[] chars = new char[length];
			java.util.Arrays.fill(chars, chr);
			string.getChars(0, string.length(), chars, left ? length - string.length() : 0);
			return new String(chars);
		}
		return string;
	}

	/**
	 * Realiza downsampling em um buffer de áudio usando coeficientes de filtro
	 * (FIR).
	 * Converte 4 amostras de entrada em 2 amostras de saída (downsample por 2).
	 *
	 * @param buf    O buffer de áudio (intercalado L/R). A entrada está nas
	 *               primeiras `count*4` posições,
	 *               a saída é escrita nas primeiras `count*2` posições.
	 * @param count  O número de pares de amostras de saída a serem produzidos.
	 * @param coeffs Os coeficientes do filtro FIR.
	 */
	public static void downsample(int[] buf, int count, int[] coeffs) {
		for (int idx = 0; idx < count; idx++) { // Para cada par de amostras de saída.
			int lamp = 0; // Amostra esquerda.
			int ramp = 0; // Amostra direita.
			// Aplica o filtro FIR.
			for (int coef = 0; coef < coeffs.length; coef++) {
				// idx*4 é o início do bloco de 4 amostras de entrada para este par de saída.
				// coef*2 porque o filtro opera em amostras mono, e o buffer é estéreo.
				lamp += (buf[idx * 4 + coef * 2] * coeffs[coef]) >> FIXED_POINT_SHIFT;
				ramp += (buf[idx * 4 + coef * 2 + 1] * coeffs[coef]) >> FIXED_POINT_SHIFT;
			}
			buf[idx * 2] = lamp;
			buf[idx * 2 + 1] = ramp;
		}
	}

	/**
	 * Aplica um efeito simples de reverb (feedback delay network).
	 *
	 * @param buf       O buffer de áudio de entrada/saída (intercalado L/R).
	 * @param reverbBuf O buffer de delay para o reverb.
	 * @param reverbIdx O índice atual no buffer de delay.
	 * @param count     O número de pares de amostras a processar.
	 * @return O novo índice no buffer de delay.
	 */
	public static int reverb(int[] buf, int[] reverbBuf, int reverbIdx, int count) {
		for (int idx = 0; idx < count; idx++) {
			// Mixa o áudio atual com o áudio atrasado do buffer de reverb.
			buf[idx * 2] = (buf[idx * 2] * 3 + reverbBuf[reverbIdx + 1]) >> 2; // Feedback com atenuação.
			buf[idx * 2 + 1] = (buf[idx * 2 + 1] * 3 + reverbBuf[reverbIdx]) >> 2;
			// Armazena o áudio processado de volta no buffer de reverb para o próximo
			// ciclo.
			reverbBuf[reverbIdx] = buf[idx * 2];
			reverbBuf[reverbIdx + 1] = buf[idx * 2 + 1];
			reverbIdx += 2; // Avança o índice no buffer de reverb.
			if (reverbIdx >= reverbBuf.length) { // Envolve o índice se atingir o fim.
				reverbIdx = 0;
			}
		}
		return reverbIdx;
	}

	/**
	 * Converte amostras de áudio de inteiros de 32 bits para bytes de 16 bits
	 * (PCM), aplicando clipping.
	 *
	 * @param inputBuf  O buffer de entrada com amostras de 32 bits (intercaladas
	 *                  L/R).
	 * @param outputBuf O buffer de saída para amostras de 16 bits (little-endian,
	 *                  intercaladas L/R).
	 * @param count     O número de amostras mono a processar (metade do tamanho do
	 *                  inputBuf se estéreo).
	 */
	public static void clip(int[] inputBuf, byte[] outputBuf, int count) {
		for (int idx = 0; idx < count; idx++) { // Processa 'count' amostras mono.
			int ampl = inputBuf[idx];
			// Aplica clipping para o intervalo de 16 bits signed.
			if (ampl > 32767) {
				ampl = 32767;
			}
			if (ampl < -32768) {
				ampl = -32768;
			}
			// Converte para bytes (little-endian).
			outputBuf[idx * 2] = (byte) ampl; // Byte menos significativo.
			outputBuf[idx * 2 + 1] = (byte) (ampl >> 8); // Byte mais significativo.
		}
	}

	/**
	 * Método principal para demonstração e teste, reproduz um arquivo MOD fornecido
	 * como argumento.
	 *
	 * @param args Argumentos da linha de comando, esperando o caminho do arquivo
	 *             MOD em args[0].
	 * @throws Exception Se ocorrer um erro durante a leitura ou reprodução.
	 */
	public static void main(String[] args) throws Exception {
		// Linhas comentadas para gerar tabelas (provavelmente usadas durante o
		// desenvolvimento original).
		// for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow(
		// 2, ( 8 - idx ) / 96.0 ) * FIXED_POINT_ONE ) );
		// for( int idx = 0; idx < 16; idx++ ) System.out.println( Math.round( Math.pow(
		// 2, idx / -12.0 ) * FIXED_POINT_ONE ) );

		if (args.length == 0) {
			System.err.println("Uso: java com.univasf.magiccube3d.util.ModPlay3 <arquivo.mod>");
			return;
		}

		final int SAMPLING_RATE = 48000; // Taxa de amostragem para a saída de áudio.
		ModPlay3 modPlay3 = null; // Instância do reprodutor.
		java.io.InputStream inputStream = null;

		// Tenta carregar como Protracker, depois como Soundtracker se falhar.
		try {
			inputStream = new java.io.FileInputStream(args[0]);
			modPlay3 = new ModPlay3(inputStream, false); // Tenta como Protracker.
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage() + " Assumindo formato Ultimate Soundtracker.");
			if (inputStream != null)
				inputStream.close(); // Fecha o stream anterior.
			// Tenta novamente como Soundtracker.
			inputStream = new java.io.FileInputStream(args[0]);
			modPlay3 = new ModPlay3(inputStream, true);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}

		System.out.println("ModPlay3 Adaptado (C)2025 Gabriel \"Snywy\" Furtado!");
		System.out.println("Baseado no código original de Martin Cameron (martincameron/tracker3).");
		System.out.println("Tocando: " + pad(modPlay3.songName, ' ', 20, false) + " Comp  Loop");

		// Imprime informações sobre os instrumentos.
		for (int idx = 1; idx < MAX_SAMPLES && modPlay3.instrumentNames[idx] != null; idx++) {
			if (modPlay3.sampleData[idx].length > 0 || modPlay3.instrumentNames[idx].length() > 0) {
				int loop = modPlay3.sampleLoopLength[idx] >> FIXED_POINT_SHIFT;
				int len = (modPlay3.sampleLoopStart[idx] >> FIXED_POINT_SHIFT) + loop;
				System.out.println(pad(String.valueOf(idx), '0', 2, true) + ' '
						+ pad(modPlay3.instrumentNames[idx], ' ', 23, false)
						+ pad(String.valueOf(len), ' ', 7, true) + pad(String.valueOf(loop), ' ', 7, true));
			}
		}

		// Configura o formato de áudio para Java Sound API.
		javax.sound.sampled.AudioFormat audioFormat = new javax.sound.sampled.AudioFormat(SAMPLING_RATE, 16, 2, true,
				false); // 48kHz, 16-bit, Estéreo, Signed, Little-Endian.
		// Obtém uma linha de dados de áudio para reprodução.
		javax.sound.sampled.SourceDataLine sourceDataLine = (javax.sound.sampled.SourceDataLine) javax.sound.sampled.AudioSystem
				.getLine(new javax.sound.sampled.DataLine.Info(
						javax.sound.sampled.SourceDataLine.class, audioFormat));
		sourceDataLine.open(audioFormat, SAMPLING_RATE / 5); // Abre e inicia a linha de áudio, buffer para ~1/5 seg.
		sourceDataLine.start();

		// Buffers para processamento de áudio.
		final int DOWNSAMPLE_BUF_SAMPLES = 2048; // Número de amostras estéreo após downsampling.
		byte[] outBuf = new byte[DOWNSAMPLE_BUF_SAMPLES * 2 * 2]; // Buffer de saída para Java Sound (16-bit estéreo). (*2
																															// para bytes, *2 para estéreo)
		int[] reverbBuf = new int[(SAMPLING_RATE / 20) * 2]; // Buffer para reverb (~50ms de delay).
		// Buffer para downsampling: precisa de espaço para amostras de entrada e
		// sobreposição do filtro.
		// Entrada para downsample é SAMPLING_RATE*2, então DOWNSAMPLE_BUF_SAMPLES é
		// após downsample por 2.
		// Portanto, o buffer de entrada para downsample precisa de
		// DOWNSAMPLE_BUF_SAMPLES*2 amostras estéreo.
		int[] downsampleBuf = new int[(DOWNSAMPLE_BUF_SAMPLES * 2 + FILTER_COEFFS.length) * 2]; // *2 para estéreo.
		// Buffer para áudio gerado pelo ModPlay3 (em SAMPLING_RATE*2 para
		// downsampling).
		int[] mixBuf = new int[SAMPLING_RATE * 2 / 5 * 2]; // ~1/5 seg de áudio a SAMPLING_RATE*2. (*2 para estéreo)

		int mixIdx = 0, mixLen = 0, reverbIdx = 0;

		// Loop principal de reprodução.
		while (true) {
			// Preserva o final do buffer de downsample para a sobreposição do filtro FIR.
			System.arraycopy(downsampleBuf, DOWNSAMPLE_BUF_SAMPLES * 2 * 2, downsampleBuf, 0, FILTER_COEFFS.length * 2);
			int offset = FILTER_COEFFS.length; // Offset no downsampleBuf para novas amostras.
			int length = offset + DOWNSAMPLE_BUF_SAMPLES * 2; // Número de amostras estéreo necessárias para downsample.

			// Preenche o buffer de downsample com áudio do mixBuf.
			while (offset < length) {
				if (mixIdx >= mixLen) { // Se mixBuf estiver vazio, gera mais áudio do ModPlay3.
					// Gera áudio em uma taxa de amostragem dobrada (para posterior downsampling).
					mixLen = modPlay3.getAudio(SAMPLING_RATE * 2, mixBuf);
					mixIdx = 0;
					if (mixLen == 0 && modPlay3.getSequencer() == false) { // Fim da música e sequenciador parado.
						break; // Sai do loop de preenchimento.
					}
				}
				if (mixLen == 0)
					break; // Evita loop infinito se getAudio retornar 0.

				int count = length - offset; // Quantas amostras estéreo ainda são necessárias.
				if (count > mixLen - mixIdx) { // Não pegar mais do que disponível em mixBuf.
					count = mixLen - mixIdx;
				}
				// Copia áudio do mixBuf para downsampleBuf (ambos são estéreo).
				System.arraycopy(mixBuf, mixIdx * 2, downsampleBuf, offset * 2, count * 2);
				mixIdx += count;
				offset += count;
			}
			if (offset < length && modPlay3.getSequencer() == false && mixLen == 0) { // Fim da música.
				break; // Sai do loop principal.
			}

			// Aplica downsampling (de SAMPLING_RATE*2 para SAMPLING_RATE).
			// downsampleBuf contém DOWNSAMPLE_BUF_SAMPLES*2 amostras estéreo de entrada.
			// A saída será DOWNSAMPLE_BUF_SAMPLES amostras estéreo.
			downsample(downsampleBuf, DOWNSAMPLE_BUF_SAMPLES, FILTER_COEFFS);
			// Aplica reverb.
			reverbIdx = reverb(downsampleBuf, reverbBuf, reverbIdx, DOWNSAMPLE_BUF_SAMPLES);
			// Converte para 16-bit e aplica clipping.
			// downsampleBuf agora tem DOWNSAMPLE_BUF_SAMPLES amostras estéreo
			// (DOWNSAMPLE_BUF_SAMPLES*2 amostras mono).
			clip(downsampleBuf, outBuf, DOWNSAMPLE_BUF_SAMPLES * 2);
			// Envia o áudio para a placa de som.
			sourceDataLine.write(outBuf, 0, DOWNSAMPLE_BUF_SAMPLES * 2 * 2); // *2 para bytes, *2 para estéreo.
		}
		sourceDataLine.drain();
		sourceDataLine.close();
		System.out.println("Reprodução finalizada.");
	}
}
