
"use strict";

document.addEventListener("DOMContentLoaded", () => {
  // Vis HTML-innholdet når siden er lastet.
  const app = document.getElementById("app");
  if (app) app.removeAttribute("v-cloak");

  const loginView = document.getElementById("login-view");
  const dashboardView = document.getElementById("dashboard-view");
  const loginForm = document.getElementById("login-form");
  const loginMessage = document.getElementById("login-message");
  const logoutButton = document.getElementById("logout-button");

  // Demo-innlogging: viser dashboardet, men autentiserer ikke
  // brukeren sikkert. Ekte innlogging må implementeres på serveren.
  loginForm?.addEventListener("submit", (event) => {
    event.preventDefault();

    if (!loginForm.reportValidity()) return;

    loginView.hidden = true;
    dashboardView.hidden = false;
    loginMessage.textContent = "";
  });

  // Bytt mellom sidene i admin-menyen.
  const pageButtons = document.querySelectorAll(
    ".admin-sidebar [data-page]"
  );
  const contentPages = document.querySelectorAll("[data-content-page]");

  function showPage(pageName) {
    contentPages.forEach((section) => {
      section.hidden = section.dataset.contentPage !== pageName;
    });

    pageButtons.forEach((button) => {
      if (button.dataset.page === pageName) {
        button.setAttribute("aria-current", "page");
      } else {
        button.removeAttribute("aria-current");
      }
    });
  }

  pageButtons.forEach((button) => {
    button.addEventListener("click", () => {
      showPage(button.dataset.page);
    });
  });

  // Logg ut og gå tilbake til innloggingen.
  logoutButton?.addEventListener("click", () => {
    dashboardView.hidden = true;
    loginView.hidden = false;
    loginForm.reset();
    showPage("dashboard");
  });

  // Åpne skjema for nytt kurs eller ny nyhet.
  const editorDialog = document.getElementById("editor-dialog");
  const editorForm = document.getElementById("editor-form");
  const editorTitle = document.getElementById("editor-title");
  const editorKind = document.getElementById("editor-kind");
  const cancelDialog = document.getElementById("cancel-dialog");
  const itemCapacity = document.getElementById("item-capacity");

  document.querySelectorAll("[data-action]").forEach((button) => {
    button.addEventListener("click", () => {
      const kind = button.dataset.action === "new-news" ? "news" : "course";

      editorForm.reset();
      document.getElementById("editor-id").value = "";
      editorKind.value = kind;
      editorTitle.textContent = kind === "news" ? "Ny nyhet" : "Nytt kurs";

      itemCapacity.closest("label")?.removeAttribute("hidden");
      itemCapacity.hidden = kind === "news";

      if (editorDialog && !editorDialog.open) {
        editorDialog.showModal();
      }
    });
  });

  cancelDialog?.addEventListener("click", () => {
    editorDialog?.close();
  });

  editorForm?.addEventListener("submit", (event) => {
    event.preventDefault();

    const message = document.getElementById("editor-message");
    message.textContent =
      "Skjemaet fungerer, men lagring av data må kobles til serveren.";

    // Dette er foreløpig kun et grensesnitt.
    // Ingen kurs eller nyheter blir lagret permanent.
  });

  // Start på dashboard-siden.
  showPage("dashboard");
});
