# Custom Gitmojis

This document explains how to customize which Gitmojis the plugin shows, where the plugin looks for the default data, how to provide your own JSON source, and how localization works.

## Configuring a custom Gitmoji source
1. Open the plugin settings (Settings / Preferences → GitMoji).
2. Set source type to "Custom".
3. Enter the JSON URL for your custom gitmoji list in the JSON URL field.
4. Enter a localization URL template in the Localization URL field. Use `{locale}` as a placeholder that will be replaced with the selected locale (see below).

The plugin expects a JSON object with a `gitmojis` array. Each element must contain `emoji`, `code`, `description`, and `name` fields. Example:

```json
{
  "gitmojis": [
    {
      "emoji": "😄",
      "code": ":smile:",
      "description": "A happy smile",
      "name": "smile"
    },
    {
      "emoji": "✨",
      "code": ":sparkles:",
      "description": "Add new features",
      "name": "sparkles"
    }
  ]
}
```

If the HTTP request fails or returns an invalid response, the plugin will silently fall back to the bundled `gitmojis.json` file.

### Localization
- Localized translations are provided as YAML files mapping gitmoji `name` → localized description.
- Provide YAML translations keyed by `name` (not `code`). The plugin looks up translations by the `name` field from the JSON.
- If the field is left empty, description from the JSON source is used.
- The plugin uses a localization URL template that can be included with the `{locale}` token. Example:

```
https://mydomain.com/gitmojis-{locale}.yaml
```

- When the plugin loads translations it will replace `{locale}` with the selected language code and try to download that YAML file. Example replacements:
  - `en_US` → https://.../gitmojis-en_US.yaml
  - `fr_FR` → https://.../gitmojis-fr_FR.yaml
  - `zh_CN` → https://.../gitmojis-zh_CN.yaml

- Supported config language values:
  - `auto` (use system locale if supported, otherwise falls back to `en_US`)
  - `en_US`, `zh_CN`, `fr_FR`, `ru_RU`, `pt_BR`

YAML structure example:

```yaml
gitmojis:
  smile: "Sourire"
  sparkles: "Ajouter de nouvelles fonctionnalités"
```

The plugin will try to download remote YAML translations. If the network fetch or parsing fails, it falls back to bundled local YAML resources named `gitmojis-<locale>.yaml` that is shipped with the plugin or description from the json directly.

## Default data
- The plugin ships a bundled default file at [gitmojis.json](./src/main/resources/gitmojis.json). If an HTTP fetch of the configured JSON URL fails, the plugin falls back to this embedded file.
- Default localization example file can be found at [gitmojis.yaml](./src/main/resources/gitmojis.yaml).
- The default remote URL used by the plugin for Gitmoji source is https://gitmoji.dev/api/gitmojis
- There is also a built-in [Conventional Gitmoji](https://conventional-gitmoji.web.app/) option, which is reduced set of Gitmojis matching the [conventional commit](https://www.conventionalcommits.org) specification.
 

## Practical example — host custom JSON and YAML on GitHub
1. Create a repository containing `gitmojis.json` in the root and localization files named `gitmojis-fr_FR.yaml`, `gitmojis-zh_CN.yaml`, etc.
2. Use GitHub raw URLs for the two fields in the plugin settings. Example:
   - JSON URL: `https://raw.githubusercontent.com/<you>/<repo>/main/gitmojis.json`
   - Localization template: `https://raw.githubusercontent.com/<you>/<repo>/main/gitmojis-{locale}.yaml`

## Further reference
Example of conventional config repo for inspiration: https://github.com/glazrtom/conventional-gitmoji-config
