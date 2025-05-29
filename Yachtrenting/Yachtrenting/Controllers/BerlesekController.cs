using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Yachtrenting.Data;

namespace Yachtrenting.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BerlesekController : ControllerBase
    {
        private readonly YachtrentingContext _context;

        public BerlesekController(YachtrentingContext context)
        {
            _context = context;
        }

        // GET: api/Berlesek
        [HttpGet]
        public async Task<ActionResult<IEnumerable<Berles>>> GetBerles()
        {
            return await _context.Berles.ToListAsync();
        }

        // GET: api/Berlesek/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Berles>> GetBerles(int id)
        {
            var berles = await _context.Berles.FindAsync(id);

            if (berles == null)
            {
                return NotFound();
            }

            return berles;
        }

        //// PUT: api/Berlesek/5
        //// To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        //[HttpPut("{id}")]
        //public async Task<IActionResult> PutBerles(int id, Berles berles)
        //{
        //    if (id != berles.Id)
        //    {
        //        return BadRequest();
        //    }

        //    _context.Entry(berles).State = EntityState.Modified;

        //    try
        //    {
        //        await _context.SaveChangesAsync();
        //    }
        //    catch (DbUpdateConcurrencyException)
        //    {
        //        if (!BerlesExists(id))
        //        {
        //            return NotFound();
        //        }
        //        else
        //        {
        //            throw;
        //        }
        //    }

        //    return NoContent();
        //}

        // POST: api/Berlesek
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<Berles>> Create(Berles berles)
        {
            // 1. Kezdődátum nem lehet korábbi mint holnap
            if (berles.StartDate.Date <= DateTime.Today)
                return BadRequest("A bérlés kezdete nem lehet a mai nap vagy korábbi.");

            // 2. Minimum 5 nap
            var duration = (berles.EndDate - berles.StartDate).Days;
            if (duration < 5)
                return BadRequest("A bérlés időtartama legalább 5 nap kell legyen.");

            // 3. Maximum 90 nap
            if (duration > 90)
                return BadRequest("A bérlés időtartama nem lehet több mint 90 nap.");

            // 4. Ütközés ellenőrzése
            bool yachtFoglalt = await _context.Berles.AnyAsync(b =>
                b.YachtId == berles.YachtId &&
                b.StartDate < berles.EndDate &&
                berles.StartDate < b.EndDate);

            if (yachtFoglalt)
                return BadRequest("Ez a yacht már foglalt a megadott időszakban.");

            _context.Berles.Add(berles);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetBerles", new { id = berles.Id }, berles);
        }

        // DELETE: api/Berlesek/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteBerles(int id)
        {
            var berles = await _context.Berles.FindAsync(id);
            if (berles == null)
            {
                return NotFound();
            }

            _context.Berles.Remove(berles);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool BerlesExists(int id)
        {
            return _context.Berles.Any(e => e.Id == id);
        }
    }
}
