enum FaType {
  FAS = "solid",
  FAL = "light",
  FAB = "brands",
  FAR = "regular"
};

const FONTAWESOME_LOCATION = '/assets/web-components/fa-icon';

const spriteCache = new Map();

async function getSprite(type: string) {
  if (spriteCache.has(type)) {
    return spriteCache.get(type);
  }

  const retPromise = fetch(`${FONTAWESOME_LOCATION}/sprites/${type}.svg`)
    .then((res) => res.text())
    .then((str) =>  new DOMParser().parseFromString(str, "text/xml"));

  spriteCache.set(type, retPromise);

  return retPromise;
}

export class FaIcon extends HTMLElement {

  constructor() {
    super();

    this.buildSprite();
  }

  private async buildSprite() {
    try {
      const svg = await getSprite(this.type);

      const iconSymbol = svg.getElementById(this.icon);
      if (!iconSymbol) {
        console.log('Symbol not found', this.className, this.type, this.icon);
        return;
      }
      const viewBox = iconSymbol.getAttribute('viewBox');
      const path = iconSymbol.getElementsByTagName('path')[0];


      this.attachShadow({mode: 'open'}).appendChild(this.getHtmlNode(viewBox, path));
    } catch(e) {
      console.log('error building symbol');
    }
  }

  private getHtmlNode(viewBox: string, path: Element) {
    const template = document.createElement('template');

    template.innerHTML = `<style>
      :host {
        display: inline-block;
        vertical-align: middle;
        height: 1em;
        margin-top: -0.085em;
      }
      svg {
        fill: currentColor;
        height: 100%;
        margin: 0;
        padding: 0;
        vertical-align: top;
      }
      </style>
    <svg viewBox="${viewBox}"
      xmlns="http://www.w3.org/2000/svg"
      >${path.outerHTML}</svg>`;

    return template.content.cloneNode(true);
  }

  get type() {
    const classes = this.classList;

    let type;
    if (classes.contains('far')) {
      type = FaType.FAR;
    } else if (classes.contains('fab')) {
      type = FaType.FAB;
    } else if (classes.contains('fal')) {
      type = FaType.FAL;
    } else {
      type = FaType.FAS;
    }

    return type;
  }

  get icon() {
    const classListArray = Array.from(this.classList);
    const faClsIndex = classListArray.findIndex((cls) => cls.startsWith('fa-'));
    if (faClsIndex === -1) {
      return null;
    }
    return classListArray[faClsIndex].replace('fa-', '');
  }

}

export async function register() {

  customElements.define('fa-icon', FaIcon);

  return new Promise((res, rej) => {
    customElements.whenDefined('fa-icon')
      .then(() => res(), rej);
  });
}

