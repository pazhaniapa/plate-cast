package com.palmah.cafe.amirtham.utils

val MENU_EXTRACTION_PROMPT = "Extract menu data from this image into a strictly valid JSON object matching this schema:\n" +
        "{\"categories\":[{\"category_name\":\"\",\"timings\":\"\",\"items\":[{\"name\":\"\",\"price\":0.0,\"timings\":\"\",\"description\":\"\"}]}]}\n" +
        "\n" +
        "Rules:\n" +
        "1. Names vs. Descriptions:\n" +
        "   - Move parenthetical contents, combos, or ingredient lists (e.g., \"(1 Idly, 1 Dosai, 1 Vadai)\", \"(with chutney & sambar)\") into `description`.\n" +
        "   - Keep serving quantities, counts, and sizes (e.g., \"(2 Pcs)\", \"(No. 2)\", \"(Set)\", \"(Large)\") inside `name`.\n" +
        "2. Extract visible text accurately. If a field has no visible value, set it to \"\" (or 0.0 for price). Do not omit keys.\n" +
        "3. Clean price into a numeric float without currency symbols. If missing, use 0.0.\n" +
        "4. Extract separate items for different sizes/variants with distinct prices.\n" +
        "5. Return ONLY raw, valid JSON. No markdown fences (no ```json), no explanations, no prefix/suffix."