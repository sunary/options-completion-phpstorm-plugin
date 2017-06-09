Options completion PhpStorm plugin
====================

**Origin from:** [options-completion-phpstorm-plugin](https://github.com/woru/options-completion-phpstorm-plugin)

**Download:** [https://plugins.jetbrains.com/plugin/9761-options-completion-plugin](https://plugins.jetbrains.com/plugin/9761-options-completion-plugin)


Parses [phpDocumentor's hash description](https://github.com/phpDocumentor/fig-standards/blob/master/proposed/phpdoc.md#examples-12) and shows supported keys.

```php
class Element {
    /**
     * Initializes this class with the given options.
     *
     * @param array $options {
     *     @var bool   $required Whether this element is required
     *     @var string $label    The display name for this element
     * }
     *
     * @enum color {"red", "green", "blue"}
     */
    public function __construct(array $options = array())
    {
        $this->color = |
        //             | ctrl+space will show supported attributes
    }
}


new Element(['label' => 'Bob', '|' ]);
//                              | ctrl+space will show supported attributes
```
