package com.palmah.cafe.amirtham.platecase_ai

import ai.koog.http.client.KoogHttpClientException
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.LLMClientException
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.AttachmentSource
import ai.koog.prompt.message.MessagePart

/**
 * Gemini 3.1 Flash-Lite (image-capable variant). Not part of Koog's [GoogleModels][ai.koog.prompt.executor.clients.google.GoogleModels]
 * catalogue yet, so it's declared here directly with the minimum capabilities [PlateCastAiClient] needs:
 * text completion and image input.
 */
private val Gemini3_1FlashLiteImage = LLModel(
    provider = LLMProvider.Google,
    id = "gemini-3.1-flash-lite-image",
    capabilities = listOf(LLMCapability.Completion, LLMCapability.Vision.Image),
)

/**
 * Thin wrapper around Koog's [GoogleLLMClient] for sending a single prompt + image to a
 * Gemini model and getting back the text response.
 *
 * The [apiKey] must be a Google AI Studio Gemini API key. Never hardcode it here — pass it in
 * from a secure source (e.g. local.properties read at build time, or platform secure storage)
 * that is kept out of version control.
 *
 * Failures are surfaced as [PlateCastAiException], not Koog's own exception types.
 */
class PlateCastAiClient(
    apiKey: String,
    private val model: LLModel = Gemini3_1FlashLiteImage,
) : AutoCloseable {

    private val googleClient = GoogleLLMClient(
        apiKey = apiKey,
        httpClientFactory = KtorKoogHttpClient.Factory(),
    )

    /**
     * Sends [promptText] together with [images] (each labelled with its own [DigitalSignageImage.name])
     * to the configured Gemini model and returns the model's response, which may contain text,
     * generated image(s), or both.
     *
     * @param promptText Instruction sent alongside the images.
     * @param images The images to attach, each preceded in the prompt by a text label of its [DigitalSignageImage.name].
     * @throws PlateCastAiException if the Gemini API call fails.
     */
    suspend fun generateDigitalSignageImage(
        promptText: String,
        images: List<DigitalSignageImage>,
    ): PlateCastAiResponse = execute(multiImagePrompt("generate-digital-signage-image", promptText, images), model)

    /**
     * Sends [promptText] together with [imageBytes] to Gemini 3.1 Flash-Lite and returns the
     * model's response.
     *
     * @param promptText Instruction/question to send alongside the image.
     * @param imageBytes Raw bytes of the image (e.g. JPEG/PNG).
     * @param imageFormat Image format/extension, e.g. "jpeg" or "png".
     * @throws PlateCastAiException if the Gemini API call fails.
     */
    suspend fun extractMenuItems(
        promptText: String,
        imageBytes: ByteArray,
        imageFormat: String = "jpeg",
    ): PlateCastAiResponse = execute(imagePrompt("extract-menu-items", promptText, imageBytes, imageFormat), GoogleModels.Gemini3_1FlashLite)

    private fun imagePrompt(id : String, promptText: String, imageBytes: ByteArray, imageFormat: String): Prompt =
        prompt(id) {
            user {
                text(promptText)
                image(
                    AttachmentSource.Image(
                        content = AttachmentContent.Binary.Bytes(imageBytes),
                        format = imageFormat,
                    )
                )
            }
        }

    private fun multiImagePrompt(id: String, promptText: String, images: List<DigitalSignageImage>): Prompt =
        prompt(id) {
            user {

                images.forEachIndexed { index, signageImage ->
                    // A bare name here reads as narrative text, not a label — Gemini's inline-bytes image
                    // parts carry no displayName/metadata field (unlike Files API uploads), so this tag is
                    // the only signal available for tying an image to its dish. Keep it structurally
                    // distinct and aligned with the "Item N of M" wording in the surrounding prompt text.
                    text("[Reference Image ${index + 1} of ${images.size}: \"${signageImage.name}\"]")
                    image(
                        AttachmentSource.Image(
                            content = AttachmentContent.Binary.Bytes(signageImage.imageBytes),
                            format = signageImage.imageFormat,
                        )
                    )
                }
                text(promptText)
            }
        }

    private suspend fun execute(request: Prompt, model: LLModel): PlateCastAiResponse {
        try {
            val response = googleClient.execute(request, model)
            val images = response.parts
                .filterIsInstance<MessagePart.Attachment>()
                .mapNotNull { it.source as? AttachmentSource.Image }
                .mapNotNull { imageSource ->
                    val binary = imageSource.content as? AttachmentContent.Binary ?: return@mapNotNull null
                    GeneratedImage(bytes = binary.asBytes(), mimeType = imageSource.mimeType)
                }
            return PlateCastAiResponse(text = response.textContent(), images = images)
        } catch (e: LLMClientException) {
            val httpError = e.cause as? KoogHttpClientException
            throw PlateCastAiException(
                message = e.message,
                statusCode = httpError?.statusCode,
                errorBody = httpError?.errorBody,
                cause = e,
            )
        }
    }

    override fun close() {
        googleClient.close()
    }
}
