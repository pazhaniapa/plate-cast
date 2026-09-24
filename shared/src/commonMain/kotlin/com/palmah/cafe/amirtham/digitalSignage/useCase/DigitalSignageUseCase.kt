package com.palmah.cafe.amirtham.digitalSignage.useCase

import co.touchlab.kermit.Logger
import com.palmah.cafe.amirtham.common.repository.FirebaseUserRepository
import com.palmah.cafe.amirtham.common.repository.UserRepository
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageBoard
import com.palmah.cafe.amirtham.digitalSignage.model.DigitalSignageInfo
import com.palmah.cafe.amirtham.digitalSignage.model.MAX_SIGNAGE_IMAGES
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepository
import com.palmah.cafe.amirtham.digitalSignage.repository.DigitalSignageRepositoryImpl
import com.palmah.cafe.amirtham.platecase_ai.PlateCastAiResponse
import com.palmah.cafe.amirtham.utils.formatPrice
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val DEFAULT_HEADER_TITLE = "TODAY'S SPECIALS"
private const val DEFAULT_SUB_HEADER = "ORDER AT THE COUNTER"
/*private const val DEFAULT_BACKGROUND_COLOR_STYLE =
    "Matte charcoal slate (#1A1D20) with a subtle radial vignette and low-contrast vertical dividers"*/

private const val DEFAULT_BACKGROUND_COLOR_STYLE =
    "Dark wood-grain undertone or soft radial gradient fading inward."

/*private const val DEFAULT_BACKGROUND_COLOR_STYLE =
    "Deep Botanical Forest (#14241B to #1B3024)\n" +
            "\n" +
            "Style: Deep dark green with soft vertical partition lines or subtle matte foliage accents."*/

/*
private const val DEFAULT_BACKGROUND_COLOR_STYLE =
    "Deep Slate / Matte Charcoal (#181A1D to #22252A)\n" +
            "\n" +
            "Style: Faint vertical brushed texture or smooth matte gradient with a subtle dark vignette around the perimeter."
*/

class DigitalSignageUseCase(
    private val repository: DigitalSignageRepository = DigitalSignageRepositoryImpl(),
    private val userRepository: UserRepository = FirebaseUserRepository(),
) {

    suspend fun generateMenuDigitalSignageImage(images: List<DigitalSignageInfo>, menuJson: String): PlateCastAiResponse {
        require(images.isNotEmpty()) {
            "images should not be empty"
        }
        val promptText = buildMenuDigitalSignagePrompt(images, menuJson)
        Logger.d(tag = "DigitalSignageUseCase", messageString = "Digital Signage Prompt: $promptText")
        return repository.generateDigitalSignageImage(promptText, images)
    }

    suspend fun generateDigitalSignageImage(images: List<DigitalSignageInfo>, boardName : String): PlateCastAiResponse {
        require(images.isNotEmpty() && images.size <= MAX_SIGNAGE_IMAGES) {
            "generateDigitalSignageImage requires 1 to $MAX_SIGNAGE_IMAGES images, got ${images.size}"
        }
        val promptText = buildDigitalSignagePrompt(images, boardName)
        Logger.d(tag = "DigitalSignageUseCase", messageString = "Digital Signage Prompt: $promptText")
        return repository.generateDigitalSignageImage(promptText, images)
    }

    /**
     * Uploads [imageBytes] to Firebase Storage under a unique path scoped to [brand] and
     * [outlet], and returns its download URL.
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun uploadGeneratedImage(imageBytes: ByteArray, mimeType: String, brand: String, outlet: String): String {
        val path = "$brand/$outlet/${Uuid.random()}"
        repository.uploadImage(path, imageBytes, mimeType)
        return repository.getDownloadUrl(path)
    }

    /** Persists [url] under `/brand/{brandId}/outlet/{outletId}/digitalsignage`. */
    suspend fun saveDigitalSignageUrl(brand: String, outlet: String, url: String, name: String) {
        repository.saveDigitalSignageUrl(brand, outlet, url, name)
    }

    /** Digital signage boards saved under the signed-in user's brand/outlet. */
    suspend fun getDigitalSignageBoards(): List<DigitalSignageBoard> {
        val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
        return repository.getDigitalSignageBoards(brand = userInfo.brand, outlet = userInfo.outlet)
    }

    /** Deletes the board identified by [boardId] under the signed-in user's brand/outlet. */
    suspend fun deleteDigitalSignageBoard(boardId: String) {
        val userInfo = userRepository.getCurrentUserInfo() ?: error("No signed-in user found")
        repository.deleteDigitalSignageBoard(brand = userInfo.brand, outlet = userInfo.outlet, boardId = boardId)
    }

    private fun buildDigitalSignagePrompt(
        images: List<DigitalSignageInfo>,
        boardName: String,
        headerTitle: String = DEFAULT_HEADER_TITLE,
        subHeader: String = DEFAULT_SUB_HEADER,
        backgroundColorStyle: String = DEFAULT_BACKGROUND_COLOR_STYLE,
    ): String {
        if (images.size == 1) {
            return buildSingleDishPrompt(images.first(), boardName)
        }

        val dishCount = images.size

        val columnLayoutRule = when (dishCount) {
            1 -> "Single focal hero layout: Dish name at top-center, large low-resolution dish photo centered, " +
                "prominent large price tag directly below. Generous, balanced negative space on left and right margins."
            2 -> "Dual-column layout: 2 equal-width columns side-by-side with a subtle vertical divider rule " +
                "between them. Both dishes sized identically for visual symmetry."
            else -> "Triple-column layout: 3 equal-width columns side-by-side across the canvas with equal " +
                "spacing and margins."
        }

        return buildString {
            appendLine("[Role & Objective]")
            appendLine(
                "Act as an expert digital signage graphic designer. Compose a single, flat, high-resolution " +
                    "digital menu board graphic in 16:9 landscape aspect ratio using the provided food image(s) " +
                    "and metadata.",
            )
            appendLine()
            appendLine("[Input Assets & Dynamic Dish Mapping]")
            appendLine("Total Dishes: $dishCount (Range: 1 to 3)")
            appendLine()
            images.forEachIndexed { index, dish ->
                val position = index + 1
                appendLine("- Item $position of $dishCount:")
                appendLine("  - Dish Name: \"${dish.name}\"")
                appendLine("  - Price: \"${formatPrice(dish.price)}\"")
                if (dish.extraInfo.isNotBlank()) {
                    appendLine("  - Extra Info: \"${dish.extraInfo}\"")
                }
                appendLine(
                    "  - Image Reference: The image immediately following the tag " +
                        "[Reference Image $position of $dishCount: \"${dish.name}\"] " +
                        "(preserve authentic plating, food styling, and vibrant textures)",
                )
            }
            appendLine()
            appendLine("- Header / Banner:")
            appendLine("  - Title: \"$boardName\"")
            //appendLine("  - Sub-tag / Callout: \"$subHeader\" (e.g., \"ORDER AT THE COUNTER\")")
            appendLine()
            appendLine("[Dynamic Grid & Layout Rules]")
            appendLine(
                "- Orientation: Strict 2D front-facing flat lay graphic (orthogonal view, zero 3D tilt or " +
                    "perspective skew).",
            )
            appendLine("- Top Header: Full-width top banner containing $headerTitle.")
            appendLine("- Dynamic Column Division based on $dishCount:")
            appendLine("  $columnLayoutRule")
            appendLine("- Column Structure (Top to Bottom for each item):")
            appendLine("  1. Dish Name: Clear, centered, high-contrast typography.")
            appendLine("  2. Dish Photo: Isolated cutout or framed plating, centered with a soft contact drop shadow.")
            appendLine("  3. Price Tag: Bold, highly legible, centered beneath the dish photo.")
            appendLine()
            appendLine("[Styling & Aesthetics]")
            appendLine(
                "- Background Style: $backgroundColorStyle.",
            )
            appendLine(
                "- Typography: Bold, modern sans-serif geometric font, engineered for high readability from " +
                    "3–5 meters away.",
            )
            appendLine(
                "- Color Hierarchy: Crisp white or ivory for dish names, bright warm amber or yellow for " +
                    "prices, muted tones for secondary subheaders.",
            )
            appendLine()
            appendLine("[Negative Constraints & Output Rules]")
            appendLine("- Output ONLY the digital screen graphic canvas asset.")
            appendLine(
                "- DO NOT generate 3D room environments, restaurant walls, TV casings/bezels, glass glare, " +
                    "outdoor angles, or cafe tables.",
            )
            appendLine("- DO NOT hallucinate extra dishes beyond the specified $dishCount.")
            append("- DO NOT produce scrambled glyphs, misspellings, or unreadable currency symbols.")
        }
    }

    private fun buildMenuDigitalSignagePrompt(images: List<DigitalSignageInfo>, menuJson: String): String {
        return buildString {
            appendLine("Role: Professional Digital Signage Designer & Data Auditor.")
            appendLine(
                "Task: Convert the provided JSON menu data into clean, modern digital menu board " +
                    "images (16:9 aspect ratio).",
            )
            appendLine("Phase 1: Data Verification (CRITICAL)")
            appendLine("Before designing, perform an internal audit: Count the total number of items in the JSON.")
            appendLine("Ensure that every single item from the JSON is included in your design plan.")
            appendLine(
                "If the volume of items is too high for one 16:9 screen, categorize them immediately " +
                    "and plan your pagination. Do not omit any item.",
            )
            appendLine("Phase 2: Reference Image Input")
            images.forEachIndexed { index, image ->
                appendLine(
                    "- The image immediately preceded by the tag " +
                        "[Reference Image ${index + 1} of ${images.size}: \"${image.name}\"] is the " +
                        "reference image for this design.",
                )
            }
            appendLine(
                "Instruction: Analyze the reference image(s) identified above. Extract their color " +
                    "palette, typography style, and layout logic. Apply these to your design so the " +
                    "output is a faithful digital adaptation of the reference.",
            )
            appendLine("Phase 3: Visual Style & Layout")
            appendLine("Design Adaptation: Adopt the color scheme and font personality of the reference.")
            appendLine(
                "Tone: Maintain high legibility. Ensure the design feels like a premium, professional " +
                    "digital menu.",
            )
            appendLine("Hierarchy: Item names (bold/prominent), descriptions (subtle), prices (right-aligned).")
            appendLine("Operational Rules:")
            appendLine(
                "NO OMISSIONS: You are strictly forbidden from skipping items. If an item is in the " +
                    "JSON, it must be on the menu.",
            )
            appendLine(
                "PAGINATION: If the total item count exceeds the capacity of a single screen while " +
                    "maintaining readability, split the content across sequential images (e.g., Image 1, " +
                    "Image 2, etc.).",
            )
            appendLine("ACCURACY: Use ONLY the exact item names and prices provided in the JSON.")
            appendLine("Formatting: Professional 16:9 widescreen output.")
            appendLine("Menu Data (JSON):")
            append(menuJson)
        }
    }

    private fun buildSingleDishPrompt(dish: DigitalSignageInfo, boardName: String): String {
        return buildString {
            appendLine("High-impact cafe digital signage poster featuring ${dish.name}.")
            appendLine()
            appendLine("[Layout & Composition]:")
            appendLine(
                "The provided dish takes center stage, rendered with appetizing warm lighting " +
                    "details, and rich color saturation. The background cafe setting is softly defocused " +
                    "(bokeh effect) for visual depth, ensuring the food remains the primary focus.",
            )
            appendLine()
            appendLine("[Typography & Dynamic Fields]:")
            appendLine("- Header Badge: \"$boardName\" — Render exact text only.")
            appendLine("- Dish Name: \"${dish.name}\" — Prominent, bold display typography with high readability.")
            appendLine("- Price: \"${formatPrice(dish.price)}\" — Placed cleanly near the dish name using modern numerals.")
            appendLine(
                "- Tagline: \"${dish.extraInfo}\" — Render EXACT verbatim characters only. If empty or absent, " +
                    "omit this text element entirely. Do not invent, substitute, or add any extra labels, " +
                    "words, or icons.",
            )
            appendLine()
            appendLine("[Negative Constraints]:")
            appendLine("- Strict text constraint: Render ONLY the exact text enclosed in the quotes above.")
            appendLine(
                "- Zero extraneous text: Do not generate any watermarks, placeholder text, ingredients, " +
                    "calorie counts, fake badges, or decorative typography not explicitly specified.",
            )
            appendLine()
            appendLine("[Finish & Legibility]:")
            append(
                "Sleek digital signage aesthetic, polished editorial restaurant poster quality, balanced " +
                    "contrast. Add subtle dark gradient vignettes or frosted card overlays behind text zones " +
                    "for sharp contrast. Optimized for 4K digital screen viewing.",
            )
        }
    }
}
