package ua.kpi.ipze.ontology.dto.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * The configuration information for a chat completions request.
 * Completions support a wide variety of tasks and generate text that continues from or "completes"
 * provided prompt data.
 */
@Getter
@Builder
public final class ChatCompletionsOptions {

  /**
   * The collection of context messages associated with this chat completions request.
   * Typical usage begins with a chat message for the System role that provides instructions for
   * the behavior of the assistant, followed by alternating messages between the User and
   * Assistant roles.
   */
  @JsonProperty(value = "messages")
  private List<ChatRequestMessage> messages;

  /**
   * The maximum number of tokens to generate.
   */
  @JsonProperty(value = "max_tokens")
  private Integer maxTokens;

  /**
   * The sampling temperature to use that controls the apparent creativity of generated completions.
   * Higher values will make output more random while lower values will make results more focused
   * and deterministic.
   * It is not recommended to modify temperature and top_p for the same completions request as the
   * interaction of these two settings is difficult to predict.
   */
  @JsonProperty(value = "temperature")
  private Double temperature;

  /**
   * An alternative to sampling with temperature called nucleus sampling. This value causes the
   * model to consider the results of tokens with the provided probability mass. As an example, a
   * value of 0.15 will cause only the tokens comprising the top 15% of probability mass to be
   * considered.
   * It is not recommended to modify temperature and top_p for the same completions request as the
   * interaction of these two settings is difficult to predict.
   */
  @JsonProperty(value = "top_p")
  private Double topP;

  /**
   * A map between GPT token IDs and bias scores that influences the probability of specific tokens
   * appearing in a completions response. Token IDs are computed via external tokenizer tools, while
   * bias scores reside in the range of -100 to 100 with minimum and maximum values corresponding to
   * a full ban or exclusive selection of a token, respectively. The exact behavior of a given bias
   * score varies by model.
   */
  @JsonProperty(value = "logit_bias")
  private Map<String, Integer> logitBias;

  /**
   * An identifier for the caller or end user of the operation. This may be used for tracking
   * or rate-limiting purposes.
   */
  @JsonProperty(value = "user")
  private String user;

  /**
   * The number of chat completions choices that should be generated for a chat completions
   * response.
   * Because this setting can generate many completions, it may quickly consume your token quota.
   * Use carefully and ensure reasonable settings for max_tokens and stop.
   */
  @JsonProperty(value = "n")
  private Integer n;

  /**
   * A collection of textual sequences that will end completions generation.
   */
  @JsonProperty(value = "stop")
  private List<String> stop;

  /**
   * A value that influences the probability of generated tokens appearing based on their existing
   * presence in generated text.
   * Positive values will make tokens less likely to appear when they already exist and increase the
   * model's likelihood to output new topics.
   */
  @JsonProperty(value = "presence_penalty")
  private Double presencePenalty;

  /**
   * A value that influences the probability of generated tokens appearing based on their cumulative
   * frequency in generated text.
   * Positive values will make tokens less likely to appear as their frequency increases and
   * decrease the likelihood of the model repeating the same statements verbatim.
   */
  @JsonProperty(value = "frequency_penalty")
  private Double frequencyPenalty;

  /**
   * A value indicating whether chat completions should be streamed for this request.
   */
  @JsonProperty(value = "stream")
  private Boolean stream;

  /**
   * The model name to provide as part of this completions request.
   */
  @JsonProperty(value = "model")
  private String model;

  /**
   * If specified, the system will make a best effort to sample deterministically such that repeated requests with
   * the
   * same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to
   * the
   * system_fingerprint response parameter to monitor changes in the backend."
   */
  @JsonProperty(value = "seed")
  private Long seed;

  /**
   * An object specifying the format that the model must output. Used to enable JSON mode.
   */
  @JsonProperty(value = "response_format")
  private ChatCompletionsResponseFormat responseFormat;
}