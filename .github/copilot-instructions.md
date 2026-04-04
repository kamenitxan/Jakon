# Copilot Instructions

## Build & Test Commands

**Scala (SBT)** — primary build tool, Java 17 required.

```bash
# Full CI pipeline
sbt ci

# Compile only
sbt compile

# Run all tests (core module, with coverage)
sbt githubTest

# Run tests for a specific module
sbt core/test
sbt shop/test
sbt frontend/test

# Fix linting issues before CI
sbt preCI

# Development mode (hot-reloading)
sbt runDev

# Production mode
sbt runProd

# Build fat JAR
sbt jar
```

**Frontend (Vite)** — admin UI assets, built separately from Scala:

```bash
cd modules/frontend
npm run build
```

The compiled Scala.js output is copied to `modules/core/src/main/resources/static/jakon/js/scalajs.js` via the `fastOptCompileCopy` / `fullOptCompileCopy` SBT tasks.

## Architecture

Jakon is a **Scala static web generator / CMS framework** built on [Javalin](https://javalin.io/) + [Pebble templates](https://pebbletemplates.io/). It is structured as three SBT sub-modules:

| Module | Path | Purpose |
|---|---|---|
| `core` | `modules/core` | Framework core: routing, DB, admin UI, templating, tasks |
| `frontend` | `modules/frontend` | Scala.js frontend compiled to JS for the admin panel |
| `shop` | `modules/shop` | Optional e-commerce extension (Stripe integration), depends on `core` |

### Startup Flow

Users extend `JakonInit` and call `.run(args)`. The framework bootstraps via a set of overridable hooks in this order:
1. `daoSetup()` — register entity classes via `DBHelper.addDao(classOf[MyEntity])`
2. `adminControllers()` — register custom admin controllers
3. `taskSetup()` — register scheduled tasks with `TaskRunner`
4. `javalinConfig()` / `routesSetup()` / `websocketSetup()` — configure Javalin
5. `afterInit()` — post-start hook

### Entity / ORM Model

- All persistent entities extend `JakonObject` (for page-level content) or `BasicJakonObject` (for simpler records). Both extend `BaseEntity` which extends `Crud`.
- Fields exposed to the admin UI are annotated with `@JakonField` (Java annotation). Field metadata (ordering, search, disabled, required) is controlled via annotation attributes.
- Each entity class needs a corresponding `.sql` file under `src/main/resources/sql/` defining its table schema.
- `DBHelper.addDao(classOf[T])` must be called during `daoSetup()` to register an entity.
- `JakonObject` stores a `childClass` field (via Scala's `sourcecode.FullName`) to enable polymorphic loading.
- `objectSettings: ObjectSettings` on each entity configures admin list/form behaviour.

### Dynamic Pages (Pagelets)

- Dynamic endpoints are implemented as **Pagelets** — classes annotated with `@Pagelet` (or `@JsonPagelet`) that extend `AbstractPagelet` / `AbstractJsonPagelet`.
- HTTP method is declared with `@Get`, `@Post`, or `@Delete` on the handler method.
- Pagelets are discovered at startup via classpath scanning (`AnnotationScanner`).
- Templates use Pebble (`.peb` extension) loaded from the configured `templateDir`.

### Static Site Generation

- `RenderTask` runs periodically (every 10 minutes in prod) to render all published pages to static HTML in `outputDir`.
- The `DevRender` class handles on-demand re-rendering in DEVEL mode.

### Configuration

- Config is read from `jakon_config.properties` (or a file passed via `--jakonConfig=path` CLI arg).
- Key settings: `templateDir`, `staticDir`, `outputDir`, `databaseDriver`, `databaseConnPath`, `deployMode` (`DEVEL` / `PRODUCTION`), `port`, `package`.
- `Settings` object provides typed accessors for all config values.
- Deploy mode controls caching (disabled in DEVEL), CORS (open in DEVEL), and access control (enforced in PRODUCTION).

### Database

- Primary DB is **SQLite** (development/test); **MySQL** is optionally supported.
- Connection pooling via HikariCP.
- SQL is written manually; `SqlGen` generates INSERT/UPDATE statements reflectively from `@JakonField`-annotated fields.
- Database schema is initialised from `.sql` files in `src/main/resources/sql/`.

### Admin UI

- Auto-generated from registered entities; each entity gets list/create/edit/delete views.
- Custom admin controllers extend `AbstractCustomPage` or are registered via `AdminSettings.registerCustomController(...)`.
- Admin routes are prefixed with `/admin/` (`Routes.AdminPrefix`).

## Key Conventions

- **Mixed Java/Scala**: Java annotations (`@JakonField`, `@Pagelet`, `@Get`, `@Post`, validators) are used alongside Scala implementation classes. New annotations should be `.java` files; implementation classes should be `.scala`.
- **Scalac options**: `-no-indent` is enforced — **do not use significant indentation** (brace-based Scala style only).
- **Tests**: Integration tests use `HtmlUnitDriver` (Selenium) against a running Javalin server. Unit tests use ScalaTest + ScalaMock. Test working directory is set to the module root (`modules/core` or `modules/shop`). Test SQLite DB is `jakonUnitTest.sqlite`.
- **Test runners**: `UnitTestRunner` for unit tests, `TestRunner` for integration tests, `ProdTestRunner` for production-mode integration tests.
- **Scalafix**: `OrganizeImports`, `DisableSyntax`, `LeakingImplicitClassVal`, `NoValInForComprehension` rules are enforced in CI. Run `sbt preCI` before committing.
- **Logging**: Use `cz.kamenitxan.jakon.logging.Logger` (not SLF4J directly) throughout the codebase.
- **Frontend assets**: Vite builds admin CSS/JS to `modules/core/src/main/resources/static/jakon/`. Scala.js compiles to the same location via SBT tasks.
- **Template location**: Pebble templates live under `templates/` (configurable). Admin templates are in `src/main/resources/templates/admin/`.
- **Validation**: Field validation uses custom Java annotations (e.g., `@NotEmpty`, `@Email`, `@Min`) paired with `Validator` Scala implementations discovered via `@ValidatedBy`.
